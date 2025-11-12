package com.houseleasing.houseleasingmanagementsystem.controller;

import com.houseleasing.houseleasingmanagementsystem.dto.MaintenanceRequestDTO;
import com.houseleasing.houseleasingmanagementsystem.model.Contract;
import com.houseleasing.houseleasingmanagementsystem.model.House;
import com.houseleasing.houseleasingmanagementsystem.model.MaintenanceRequest;
import com.houseleasing.houseleasingmanagementsystem.model.enums.MaintenanceStatus;
import com.houseleasing.houseleasingmanagementsystem.repository.ContractRepository;
import com.houseleasing.houseleasingmanagementsystem.service.HouseService;
import com.houseleasing.houseleasingmanagementsystem.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/maintenance-requests")
public class MaintenanceRequestController {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceRequestController.class);

    @Autowired
    private MaintenanceRequestService maintenanceRequestService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private ContractRepository contractRepository;

    // DTO转Entity
    private MaintenanceRequest convertToEntity(MaintenanceRequestDTO dto) {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setId(dto.getId());
        request.setDescription(dto.getDescription());
        request.setCost(dto.getCost());
        request.setStatus(dto.getStatus());
        request.setCompletedAt(dto.getCompletedAt());
        // 从 DTO 复制提交者信息
        request.setRequesterName(dto.getRequesterName());
        request.setContact(dto.getContact());
        return request;
    }

    /**
     * 创建维修申请（租客提交）
     * 租客信息自动从房源的有效合同中获取
     */
    @PostMapping
    public ResponseEntity<?> createMaintenanceRequest(@Valid @RequestBody MaintenanceRequestDTO dto) {
        // 验证房源是否存在
        if (dto.getHouseId() == null) {
            return ResponseEntity.badRequest().body("房源ID不能为空");
        }
        House house = houseService.getHouseById(dto.getHouseId());
        if (house == null) {
            return ResponseEntity.badRequest().body("指定的房源不存在: " + dto.getHouseId());
        }

        // 从房源的有效合同中获取租客信息；如果没有活跃合同，不再拒绝请求，而是允许提交并记录警告
        Optional<Contract> activeContract = contractRepository.findActiveContractByHouseId(dto.getHouseId());
        if (activeContract.isEmpty()) {
            logger.warn("提交维修：房源 {} 没有有效的租赁合同或不在生效时间，仍允许提交维修申请（由前端提交者信息填充）", dto.getHouseId());
        } else {
            // 若需要可以在此使用 activeContract 的租客信息覆盖 DTO 中的提交者信息
            // e.g. request.setRequesterName(...)
        }

        MaintenanceRequest request = convertToEntity(dto);
        request.setHouse(house);

        MaintenanceRequest createdRequest = maintenanceRequestService.createMaintenanceRequest(request);
        return ResponseEntity.ok(createdRequest);
    }

    /**
     * 根据ID获取维修申请详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequest> getMaintenanceRequestById(@PathVariable Long id) {
        MaintenanceRequest request = maintenanceRequestService.getMaintenanceRequestById(id);
        if (request != null) {
            return ResponseEntity.ok(request);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取所有维修申请（分页，按创建时间倒序）
     */
    @GetMapping
    public ResponseEntity<Page<MaintenanceRequest>> getAllMaintenanceRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MaintenanceRequest> requests = maintenanceRequestService.getAllMaintenanceRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 根据租客ID获取维修申请（租客查看自己的申请记录）
     */
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<Page<MaintenanceRequest>> getMaintenanceRequestsByTenant(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByTenantId(tenantId, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 根据状态获取维修申请（查看待处理申请列表）
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<MaintenanceRequest>> getMaintenanceRequestsByStatus(
            @PathVariable MaintenanceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MaintenanceRequest> requests = maintenanceRequestService.getMaintenanceRequestsByStatus(status, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 搜索维修申请（支持多条件筛选：租客ID、房源ID、状态、时间范围、描述模糊搜索）
     */
    @GetMapping("/search")
    public ResponseEntity<Page<MaintenanceRequest>> searchMaintenanceRequests(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long houseId,
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MaintenanceRequest> requests = maintenanceRequestService.searchMaintenanceRequests(
                tenantId, houseId, status, description, startDate, endDate, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 更新维修状态（处理维修流程）
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateMaintenanceStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String statusStr = statusUpdate.get("status");
            if (statusStr == null || statusStr.isBlank()) {
                return ResponseEntity.badRequest().body("状态不能为空");
            }
            MaintenanceStatus status = MaintenanceStatus.valueOf(statusStr);
            MaintenanceRequest updatedRequest = maintenanceRequestService.updateMaintenanceStatus(id, status);
            if (updatedRequest != null) {
                return ResponseEntity.ok(updatedRequest);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("无效的状态值");
        }
    }

    /**
     * 更新维修费用（预估维修费用）
     */
    @PatchMapping("/{id}/cost")
    public ResponseEntity<?> updateMaintenanceCost(
            @PathVariable Long id,
            @RequestBody Map<String, Double> costUpdate) {
        Double cost = costUpdate.get("cost");
        if (cost == null || cost <= 0) {
            return ResponseEntity.badRequest().body("维修费用必须大于0");
        }
        MaintenanceRequest updatedRequest = maintenanceRequestService.updateMaintenanceCost(id, cost);
        if (updatedRequest != null) {
            return ResponseEntity.ok(updatedRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 完成维修（更新状态为已完成，并记录完成时间）
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<MaintenanceRequest> completeMaintenanceRequest(@PathVariable Long id) {
        MaintenanceRequest completedRequest = maintenanceRequestService.completeMaintenanceRequest(id);
        if (completedRequest != null) {
            return ResponseEntity.ok(completedRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 标记为已支付（更新状态为已支付）
     */
    @PatchMapping("/{id}/paid")
    public ResponseEntity<MaintenanceRequest> markMaintenanceRequestAsPaid(@PathVariable Long id) {
        MaintenanceRequest paidRequest = maintenanceRequestService.markAsPaid(id);
        if (paidRequest != null) {
            return ResponseEntity.ok(paidRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新维修申请
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMaintenanceRequest(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceRequestDTO dto) {
        MaintenanceRequest request = convertToEntity(dto);

        // 如果提供了新的房源ID，更新房源关联
        if (dto.getHouseId() != null) {
            House house = houseService.getHouseById(dto.getHouseId());
            if (house == null) {
                return ResponseEntity.badRequest().body("指定的房源不存在: " + dto.getHouseId());
            }
            request.setHouse(house);
        }


        MaintenanceRequest updatedRequest = maintenanceRequestService.updateMaintenanceRequest(id, request);
        if (updatedRequest != null) {
            return ResponseEntity.ok(updatedRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除维修申请
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenanceRequest(@PathVariable Long id) {
        MaintenanceRequest request = maintenanceRequestService.getMaintenanceRequestById(id);
        if (request != null) {
            maintenanceRequestService.deleteMaintenanceRequest(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取维修统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getMaintenanceStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 统计各状态的维修申请数量
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        long pendingCount = maintenanceRequestService.getMaintenanceRequestsByStatus(MaintenanceStatus.PENDING, pageable).getTotalElements();
        long inProgressCount = maintenanceRequestService.getMaintenanceRequestsByStatus(MaintenanceStatus.IN_PROGRESS, pageable).getTotalElements();
        long completedCount = maintenanceRequestService.getMaintenanceRequestsByStatus(MaintenanceStatus.COMPLETED, pageable).getTotalElements();

        statistics.put("pendingCount", pendingCount);
        statistics.put("inProgressCount", inProgressCount);
        statistics.put("completedCount", completedCount);
        statistics.put("totalCount", pendingCount + inProgressCount + completedCount);

        return ResponseEntity.ok(statistics);
    }
}
