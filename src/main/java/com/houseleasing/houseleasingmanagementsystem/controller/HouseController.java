package com.houseleasing.houseleasingmanagementsystem.controller;

import com.houseleasing.houseleasingmanagementsystem.dto.HouseDTO;
import com.houseleasing.houseleasingmanagementsystem.model.House;
import com.houseleasing.houseleasingmanagementsystem.model.User;
import com.houseleasing.houseleasingmanagementsystem.model.enums.HouseStatus;
import com.houseleasing.houseleasingmanagementsystem.service.HouseService;
import com.houseleasing.houseleasingmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/houses")
public class HouseController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private UserService userService;

    private House convertToEntity(HouseDTO houseDTO) {
        House house = new House();
        house.setId(houseDTO.getId());
        house.setAddress(houseDTO.getAddress());
        house.setHouseType(houseDTO.getHouseType());
        house.setArea(houseDTO.getArea());
        house.setRent(houseDTO.getRent());
        house.setDecoration(houseDTO.getDecoration());
        house.setFacilities(houseDTO.getFacilities());
        house.setStatus(houseDTO.getStatus());
        house.setDescription(houseDTO.getDescription());
        house.setRecommended(houseDTO.getRecommended());
        return house;
    }

    // 搜索房源
    @GetMapping("/search")
    public ResponseEntity<Page<House>> searchHouses(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String houseType,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea,
            @RequestParam(required = false) Double minRent,
            @RequestParam(required = false) Double maxRent,
            @RequestParam(required = false) String decoration,
            @RequestParam(required = false) String facilities,
            @RequestParam(required = false) HouseStatus status,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(required = false) Long landlordId,
            @RequestParam(required = false) String landlordName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<House> houses = houseService.searchHouses(address, houseType, minArea, maxArea,
                minRent, maxRent, decoration, facilities,
                status, recommended, landlordId, landlordName, pageable);
        return ResponseEntity.ok(houses);
    }



    // 根据ID获取房源
    @GetMapping("/{id}")
    public ResponseEntity<House> getHouseById(@PathVariable Long id) {
        House house = houseService.getHouseById(id);
        if (house != null) {
            return ResponseEntity.ok(house);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 创建房源
    @PostMapping
    public ResponseEntity<?> createHouse(@Valid @RequestBody HouseDTO houseDTO) {
        // landlordId is required for FK binding
        if (houseDTO.getLandlordId() == null) {
            return ResponseEntity.badRequest().body("landlordId不能为空");
        }
        User landlord = userService.getUserById(houseDTO.getLandlordId());
        if (landlord == null) {
            return ResponseEntity.badRequest().body("指定的房东不存在: " + houseDTO.getLandlordId());
        }
        House house = convertToEntity(houseDTO);
        house.setLandlord(landlord);
        House createdHouse = houseService.createHouse(house);
        return ResponseEntity.ok(createdHouse);
    }

    // 更新房源
    @PutMapping("/{id}")
    public ResponseEntity<House> updateHouse(@PathVariable Long id, @Valid @RequestBody HouseDTO houseDTO) {
        House house = convertToEntity(houseDTO);
        // landlordId provided -> update landlord; if not provided, service keeps existing landlord
        if (houseDTO.getLandlordId() != null) {
            User landlord = userService.getUserById(houseDTO.getLandlordId());
            if (landlord == null) {
                return ResponseEntity.badRequest().build();
            }
            house.setLandlord(landlord);
        }
        House updatedHouse = houseService.updateHouse(id, house);
        if (updatedHouse != null) {
            return ResponseEntity.ok(updatedHouse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    // 删除房源
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHouse(@PathVariable Long id) {
        houseService.deleteHouse(id);
        return ResponseEntity.ok().build();
    }

}
