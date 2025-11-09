package com.houseleasing.houseleasingmanagementsystem.service.impl;

import com.houseleasing.houseleasingmanagementsystem.model.Contract;
import com.houseleasing.houseleasingmanagementsystem.model.enums.ContractStatus;
import com.houseleasing.houseleasingmanagementsystem.repository.ContractRepository;
import com.houseleasing.houseleasingmanagementsystem.service.ContractService;
import com.houseleasing.houseleasingmanagementsystem.service.RentPaymentService;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private RentPaymentService rentPaymentService;

    @Override
    public Contract createContract(Contract contract) {
        if (contract.getStatus() == null) {
            contract.setStatus(ContractStatus.DRAFT);
        }
        Contract saved = contractRepository.save(contract);
        // 生成租金计划，第一期设为已付款（根据你的描述）
        rentPaymentService.generateScheduleForContract(saved, true);
        return getContractById(saved.getId());
    }

    @Override
    public Contract updateContract(Long id, Contract contractDetails) {
        Contract c = getContractById(id);
        if (c == null) return null;
        c.setContractNo(contractDetails.getContractNo());
        c.setHouse(contractDetails.getHouse());
        c.setTenant(contractDetails.getTenant());
        c.setLandlord(contractDetails.getLandlord());
        c.setStartDate(contractDetails.getStartDate());
        c.setEndDate(contractDetails.getEndDate());
        c.setRentAmount(contractDetails.getRentAmount());
        c.setPaymentCycle(contractDetails.getPaymentCycle()); // apply cycle changes
        c.setPaymentMethod(contractDetails.getPaymentMethod());
        c.setBreachClause(contractDetails.getBreachClause());
        c.setStatus(contractDetails.getStatus());
        Contract saved = contractRepository.save(c);
        return getContractById(saved.getId());
    }

    @Override
    public void deleteContract(Long id) {
        contractRepository.deleteById(id);
    }

    @Override
    public Contract getContractById(Long id) {
        Optional<Contract> c = contractRepository.findByIdWithRelations(id);
        return c.orElse(null);
    }

    @Override
    public Contract getByContractNo(String contractNo) {
        return contractRepository.findByContractNo(contractNo);
    }

    @Override
    public Page<Contract> searchContracts(String contractNo,
                                          Long houseId,
                                          Long landlordId,
                                          Long tenantId,
                                          ContractStatus status,
                                          LocalDate startDateFrom,
                                          LocalDate startDateTo,
                                          LocalDate endDateFrom,
                                          LocalDate endDateTo,
                                          Pageable pageable) {
        Specification<Contract> spec = (root, query, cb) -> {
            if (query != null && Contract.class.equals(query.getResultType())) {
                root.fetch("house", JoinType.LEFT);
                root.fetch("landlord", JoinType.LEFT);
                root.fetch("tenant", JoinType.LEFT);
            }
            List<Predicate> ps = new ArrayList<>();
            if (contractNo != null && !contractNo.isBlank()) {
                ps.add(cb.like(root.get("contractNo"), "%" + contractNo + "%"));
            }
            if (houseId != null) {
                ps.add(cb.equal(root.get("house").get("id"), houseId));
            }
            if (landlordId != null) {
                ps.add(cb.equal(root.get("landlord").get("id"), landlordId));
            }
            if (tenantId != null) {
                ps.add(cb.equal(root.get("tenant").get("id"), tenantId));
            }
            if (status != null) {
                ps.add(cb.equal(root.get("status"), status));
            }
            if (startDateFrom != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
            }
            if (startDateTo != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("startDate"), startDateTo));
            }
            if (endDateFrom != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("endDate"), endDateFrom));
            }
            if (endDateTo != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("endDate"), endDateTo));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return contractRepository.findAll(spec, pageable);
    }
}
