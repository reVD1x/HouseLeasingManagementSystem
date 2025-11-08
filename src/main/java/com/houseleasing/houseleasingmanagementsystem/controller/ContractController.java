package com.houseleasing.houseleasingmanagementsystem.controller;

import com.houseleasing.houseleasingmanagementsystem.model.Contract;
import com.houseleasing.houseleasingmanagementsystem.model.House;
import com.houseleasing.houseleasingmanagementsystem.model.User;
import com.houseleasing.houseleasingmanagementsystem.model.enums.ContractStatus;
import com.houseleasing.houseleasingmanagementsystem.service.ContractService;
import com.houseleasing.houseleasingmanagementsystem.service.HouseService;
import com.houseleasing.houseleasingmanagementsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private UserService userService;

    // 生成合同模板所需数据：根据传入 houseId/landlordId/tenantId 可返回基础信息（前端自行渲染模板）
    @GetMapping("/template-data")
    public ResponseEntity<Contract> getTemplateData(@RequestParam Long houseId,
                                                    @RequestParam Long landlordId,
                                                    @RequestParam Long tenantId) {
        House house = houseService.getHouseById(houseId);
        User landlord = userService.getUserById(landlordId);
        User tenant = userService.getUserById(tenantId);
        if (house == null || landlord == null || tenant == null) {
            return ResponseEntity.badRequest().build();
        }
        Contract c = new Contract();
        c.setHouse(house);
        c.setLandlord(landlord);
        c.setTenant(tenant);
        return ResponseEntity.ok(c);
    }

    // 在线签订（创建）合同
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Contract contract) {
        // 解析并绑定完整的关联实体，避免只传 {id} 导致关联对象无字段
        if (contract.getHouse() == null || contract.getHouse().getId() == null ||
                contract.getLandlord() == null || contract.getLandlord().getId() == null ||
                contract.getTenant() == null || contract.getTenant().getId() == null) {
            return ResponseEntity.badRequest().body("house/landlord/tenant 的 id 必须提供");
        }
        House house = houseService.getHouseById(contract.getHouse().getId());
        User landlord = userService.getUserById(contract.getLandlord().getId());
        User tenant = userService.getUserById(contract.getTenant().getId());
        if (house == null || landlord == null || tenant == null) {
            return ResponseEntity.badRequest().body("house/landlord/tenant 不存在");
        }
        contract.setHouse(house);
        contract.setLandlord(landlord);
        contract.setTenant(tenant);
        Contract saved = contractService.createContract(contract);
        return ResponseEntity.ok(saved);
    }

    // 更新合同
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Contract contract) {
        // 若传了关联 id，则也做实体绑定
        if (contract.getHouse() != null && contract.getHouse().getId() != null) {
            House house = houseService.getHouseById(contract.getHouse().getId());
            if (house == null) return ResponseEntity.badRequest().body("house 不存在");
            contract.setHouse(house);
        }
        if (contract.getLandlord() != null && contract.getLandlord().getId() != null) {
            User landlord = userService.getUserById(contract.getLandlord().getId());
            if (landlord == null) return ResponseEntity.badRequest().body("landlord 不存在");
            contract.setLandlord(landlord);
        }
        if (contract.getTenant() != null && contract.getTenant().getId() != null) {
            User tenant = userService.getUserById(contract.getTenant().getId());
            if (tenant == null) return ResponseEntity.badRequest().body("tenant 不存在");
            contract.setTenant(tenant);
        }
        Contract updated = contractService.updateContract(id, contract);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // 获取合同详情
    @GetMapping("/{id}")
    public ResponseEntity<Contract> getById(@PathVariable Long id) {
        Contract c = contractService.getContractById(id);
        return c != null ? ResponseEntity.ok(c) : ResponseEntity.notFound().build();
    }

    // 删除合同
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok().build();
    }

    // 合同查询（分页）
    @GetMapping("/search")
    public ResponseEntity<Page<Contract>> search(@RequestParam(required = false) String contractNo,
                                                 @RequestParam(required = false) Long houseId,
                                                 @RequestParam(required = false) Long landlordId,
                                                 @RequestParam(required = false) Long tenantId,
                                                 @RequestParam(required = false) ContractStatus status,
                                                 @RequestParam(required = false) LocalDate startDateFrom,
                                                 @RequestParam(required = false) LocalDate startDateTo,
                                                 @RequestParam(required = false) LocalDate endDateFrom,
                                                 @RequestParam(required = false) LocalDate endDateTo,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Contract> res = contractService.searchContracts(contractNo, houseId, landlordId, tenantId,
                status, startDateFrom, startDateTo, endDateFrom, endDateTo, pageable);
        return ResponseEntity.ok(res);
    }
}
