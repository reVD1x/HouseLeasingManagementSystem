package com.houseleasing.houseleasingmanagementsystem.service;

import com.houseleasing.houseleasingmanagementsystem.model.MaintenanceRequest;
import com.houseleasing.houseleasingmanagementsystem.model.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface MaintenanceRequestService {

    // 创建维修申请
    MaintenanceRequest createMaintenanceRequest(MaintenanceRequest maintenanceRequest);

    // 根据ID获取维修申请
    MaintenanceRequest getMaintenanceRequestById(Long id);

    // 获取所有维修申请（分页）
    Page<MaintenanceRequest> getAllMaintenanceRequests(Pageable pageable);

    // 根据租客ID获取维修申请（分页）
    Page<MaintenanceRequest> getMaintenanceRequestsByTenantId(Long tenantId, Pageable pageable);

    // 根据状态获取维修申请（分页）
    Page<MaintenanceRequest> getMaintenanceRequestsByStatus(MaintenanceStatus status, Pageable pageable);

    // 根据租客ID和状态获取维修申请（分页）
    Page<MaintenanceRequest> getMaintenanceRequestsByTenantIdAndStatus(Long tenantId, MaintenanceStatus status, Pageable pageable);

    // 搜索维修申请（支持多条件筛选，包括描述模糊搜索）
    Page<MaintenanceRequest> searchMaintenanceRequests(
            Long tenantId,
            Long houseId,
            MaintenanceStatus status,
            String description,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // 更新维修状态
    MaintenanceRequest updateMaintenanceStatus(Long id, MaintenanceStatus status);

    // 更新维修费用
    MaintenanceRequest updateMaintenanceCost(Long id, Double cost);

    // 完成维修（更新状态为已完成，并记录完成时间）
    MaintenanceRequest completeMaintenanceRequest(Long id);

    // 标记为已支付（更新状态为已支付）
    MaintenanceRequest markAsPaid(Long id);

    // 更新维修申请
    MaintenanceRequest updateMaintenanceRequest(Long id, MaintenanceRequest maintenanceRequest);

    // 删除维修申请
    void deleteMaintenanceRequest(Long id);
}

