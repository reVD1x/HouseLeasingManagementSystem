package com.houseleasing.houseleasingmanagementsystem.service.impl;

import com.houseleasing.houseleasingmanagementsystem.model.MaintenanceRequest;
import com.houseleasing.houseleasingmanagementsystem.model.enums.MaintenanceStatus;
import com.houseleasing.houseleasingmanagementsystem.repository.MaintenanceRequestRepository;
import com.houseleasing.houseleasingmanagementsystem.service.MaintenanceRequestService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Override
    public MaintenanceRequest createMaintenanceRequest(MaintenanceRequest maintenanceRequest) {
        // 默认状态为待处理
        if (maintenanceRequest.getStatus() == null) {
            maintenanceRequest.setStatus(MaintenanceStatus.PENDING);
        }
        return maintenanceRequestRepository.save(maintenanceRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRequest getMaintenanceRequestById(Long id) {
        Optional<MaintenanceRequest> maintenanceRequest = maintenanceRequestRepository.findByIdWithHouse(id);
        return maintenanceRequest.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRequest> getAllMaintenanceRequests(Pageable pageable) {
        return maintenanceRequestRepository.findAllWithHouse(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRequest> getMaintenanceRequestsByTenantId(Long tenantId, Pageable pageable) {
        return maintenanceRequestRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRequest> getMaintenanceRequestsByStatus(MaintenanceStatus status, Pageable pageable) {
        return maintenanceRequestRepository.findByStatusWithHouse(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRequest> getMaintenanceRequestsByTenantIdAndStatus(Long tenantId, MaintenanceStatus status, Pageable pageable) {
        return maintenanceRequestRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRequest> searchMaintenanceRequests(
            Long tenantId,
            Long houseId,
            MaintenanceStatus status,
            String description,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        Specification<MaintenanceRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tenantId != null) {
                // 通过房源的有效合同关联租客
                var contractJoin = root.join("house").join("contracts");
                predicates.add(cb.equal(contractJoin.get("tenant").get("id"), tenantId));
                predicates.add(cb.equal(contractJoin.get("status"), "ACTIVE"));
            }

            if (houseId != null) {
                predicates.add(cb.equal(root.get("house").get("id"), houseId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (description != null && !description.isBlank()) {
                predicates.add(cb.like(root.get("description"), "%" + description + "%"));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return maintenanceRequestRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public MaintenanceRequest updateMaintenanceStatus(Long id, MaintenanceStatus status) {
        MaintenanceRequest maintenanceRequest = getMaintenanceRequestById(id);
        if (maintenanceRequest != null) {
            maintenanceRequest.setStatus(status);

            // 如果状态变更为已完成或已支付，自动记录完成时间（如果还没有记录）
            if ((status == MaintenanceStatus.COMPLETED || status == MaintenanceStatus.PAID)
                    && maintenanceRequest.getCompletedAt() == null) {
                maintenanceRequest.setCompletedAt(LocalDateTime.now());
            }

            return maintenanceRequestRepository.save(maintenanceRequest);
        }
        return null;
    }

    @Override
    @Transactional
    public MaintenanceRequest updateMaintenanceCost(Long id, Double cost) {
        MaintenanceRequest maintenanceRequest = getMaintenanceRequestById(id);
        if (maintenanceRequest != null) {
            maintenanceRequest.setCost(cost);
            return maintenanceRequestRepository.save(maintenanceRequest);
        }
        return null;
    }

    @Override
    @Transactional
    public MaintenanceRequest completeMaintenanceRequest(Long id) {
        MaintenanceRequest maintenanceRequest = getMaintenanceRequestById(id);
        if (maintenanceRequest != null) {
            maintenanceRequest.setStatus(MaintenanceStatus.COMPLETED);
            maintenanceRequest.setCompletedAt(LocalDateTime.now());
            return maintenanceRequestRepository.save(maintenanceRequest);
        }
        return null;
    }

    @Override
    @Transactional
    public MaintenanceRequest markAsPaid(Long id) {
        MaintenanceRequest maintenanceRequest = getMaintenanceRequestById(id);
        if (maintenanceRequest != null) {
            maintenanceRequest.setStatus(MaintenanceStatus.PAID);
            return maintenanceRequestRepository.save(maintenanceRequest);
        }
        return null;
    }

    @Override
    @Transactional
    public MaintenanceRequest updateMaintenanceRequest(Long id, MaintenanceRequest maintenanceRequestDetails) {
        MaintenanceRequest maintenanceRequest = getMaintenanceRequestById(id);
        if (maintenanceRequest != null) {
            if (maintenanceRequestDetails.getDescription() != null) {
                maintenanceRequest.setDescription(maintenanceRequestDetails.getDescription());
            }
            if (maintenanceRequestDetails.getCost() != null) {
                maintenanceRequest.setCost(maintenanceRequestDetails.getCost());
            }
            if (maintenanceRequestDetails.getStatus() != null) {
                maintenanceRequest.setStatus(maintenanceRequestDetails.getStatus());

                // 如果状态变更为已完成或已支付，自动记录完成时间（如果还没有记录）
                if ((maintenanceRequestDetails.getStatus() == MaintenanceStatus.COMPLETED
                        || maintenanceRequestDetails.getStatus() == MaintenanceStatus.PAID)
                        && maintenanceRequest.getCompletedAt() == null) {
                    maintenanceRequest.setCompletedAt(LocalDateTime.now());
                }
            }
            if (maintenanceRequestDetails.getCompletedAt() != null) {
                maintenanceRequest.setCompletedAt(maintenanceRequestDetails.getCompletedAt());
            }
            // 保留原有的房源关联，除非明确提供新的
            if (maintenanceRequestDetails.getHouse() != null) {
                maintenanceRequest.setHouse(maintenanceRequestDetails.getHouse());
            }

            return maintenanceRequestRepository.save(maintenanceRequest);
        }
        return null;
    }

    @Override
    public void deleteMaintenanceRequest(Long id) {
        maintenanceRequestRepository.deleteById(id);
    }
}

