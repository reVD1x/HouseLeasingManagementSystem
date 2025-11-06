package com.houseleasing.houseleasingmanagementsystem.service.impl;

import com.houseleasing.houseleasingmanagementsystem.model.House;
import com.houseleasing.houseleasingmanagementsystem.model.enums.HouseStatus;
import com.houseleasing.houseleasingmanagementsystem.repository.HouseRepository;
import com.houseleasing.houseleasingmanagementsystem.service.HouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HouseServiceImpl implements HouseService {

    @Autowired
    private HouseRepository houseRepository;

    @Override
    public Page<House> getAllHouses(Pageable pageable) {
        return houseRepository.findAll(pageable);
    }

    @Override
    public House getHouseById(Long id) {
        Optional<House> house = houseRepository.findById(id);
        return house.orElse(null);
    }

    @Override
    public House createHouse(House house) {
        return houseRepository.save(house);
    }

    @Override
    public House updateHouse(Long id, House houseDetails) {
        House house = getHouseById(id);
        if (house != null) {
            house.setAddress(houseDetails.getAddress());
            house.setHouseType(houseDetails.getHouseType());
            house.setArea(houseDetails.getArea());
            house.setRent(houseDetails.getRent());
            house.setDecoration(houseDetails.getDecoration());
            house.setFacilities(houseDetails.getFacilities());
            house.setDescription(houseDetails.getDescription());
            house.setStatus(houseDetails.getStatus());
            house.setRecommended(houseDetails.getRecommended());
            house.setLandlord(houseDetails.getLandlord());
            return houseRepository.save(house);
        }
        return null;
    }

    @Override
    public void deleteHouse(Long id) {
        houseRepository.deleteById(id);
    }

    @Override
    public Page<House> searchHouses(String address, String houseType, Double minRent, Double maxRent, HouseStatus status, Pageable pageable) {
        return houseRepository.findByConditions(address, houseType, minRent, maxRent, status, pageable);
    }


    @Override
    public List<House> getRecommendedHouses() {
        return houseRepository.findByRecommendedTrue();
    }

    @Override
    public House toggleRecommendation(Long id) {
        House house = getHouseById(id);
        if (house != null) {
            house.setRecommended(!house.getRecommended());
            return houseRepository.save(house);
        }
        return null;
    }
}
