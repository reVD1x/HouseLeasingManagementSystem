package com.houseleasing.houseleasingmanagementsystem.repository;

import com.houseleasing.houseleasingmanagementsystem.model.MaintenanceRequest;
import com.houseleasing.houseleasingmanagementsystem.model.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long>, JpaSpecificationExecutor<MaintenanceRequest> {

    // 根据ID查询单个维修申请（JOIN FETCH加载关联的house）
    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.house " +
           "WHERE mr.id = :id")
    Optional<MaintenanceRequest> findByIdWithHouse(@Param("id") Long id);

    // 分页查询所有维修申请（JOIN FETCH）
    @Query(value = "SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.house",
           countQuery = "SELECT COUNT(mr) FROM MaintenanceRequest mr")
    Page<MaintenanceRequest> findAllWithHouse(Pageable pageable);

    // 根据状态查询维修申请（JOIN FETCH）
    @Query(value = "SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.house " +
           "WHERE mr.status = :status",
           countQuery = "SELECT COUNT(mr) FROM MaintenanceRequest mr WHERE mr.status = :status")
    Page<MaintenanceRequest> findByStatusWithHouse(@Param("status") MaintenanceStatus status, Pageable pageable);

    // 根据租客ID查询维修申请（通过房源的有效合同关联）
    // 根据租客ID查询维修申请（通过房源的有效合同关联）
    @Query(value = "SELECT mr FROM MaintenanceRequest mr " +
            "LEFT JOIN FETCH mr.house h " +
            "JOIN Contract c ON c.house.id = h.id " +
            "WHERE c.tenant.id = :tenantId " +
            "AND c.status = 'ACTIVE' " +
            "AND mr.createdAt BETWEEN c.startDate AND c.endDate",
            countQuery = "SELECT COUNT(mr) FROM MaintenanceRequest mr " +
            "JOIN Contract c ON c.house.id = mr.house.id " +
            "WHERE c.tenant.id = :tenantId " +
            "AND c.status = 'ACTIVE' " +
            "AND mr.createdAt BETWEEN c.startDate AND c.endDate")
    Page<MaintenanceRequest> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    // 根据状态查询维修申请
    Page<MaintenanceRequest> findByStatus(MaintenanceStatus status, Pageable pageable);

    // 根据租客ID和状态查询维修申请（通过房源的有效合同关联）
    @Query(value = "SELECT mr FROM MaintenanceRequest mr " +
            "LEFT JOIN FETCH mr.house h " +
            "JOIN Contract c ON c.house.id = h.id " +
            "WHERE c.tenant.id = :tenantId " +
            "AND c.status = 'ACTIVE' " +
            "AND mr.status = :status " +
            "AND mr.createdAt BETWEEN c.startDate AND c.endDate",
            countQuery = "SELECT COUNT(mr) FROM MaintenanceRequest mr " +
            "JOIN Contract c ON c.house.id = mr.house.id " +
            "WHERE c.tenant.id = :tenantId " +
            "AND c.status = 'ACTIVE' " +
            "AND mr.status = :status " +
            "AND mr.createdAt BETWEEN c.startDate AND c.endDate")
    Page<MaintenanceRequest> findByTenantIdAndStatus(@Param("tenantId") Long tenantId,
                                                       @Param("status") MaintenanceStatus status,
                                                       Pageable pageable);

    // 根据房源ID查询维修申请
    @Query("SELECT mr FROM MaintenanceRequest mr " +
           "LEFT JOIN FETCH mr.house " +
           "WHERE mr.house.id = :houseId")
    List<MaintenanceRequest> findByHouse_Id(@Param("houseId") Long houseId);
}

