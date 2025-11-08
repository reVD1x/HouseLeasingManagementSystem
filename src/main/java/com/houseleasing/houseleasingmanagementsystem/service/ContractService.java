package com.houseleasing.houseleasingmanagementsystem.service;

import com.houseleasing.houseleasingmanagementsystem.model.Contract;
import com.houseleasing.houseleasingmanagementsystem.model.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ContractService {
    Contract createContract(Contract contract);

    Contract updateContract(Long id, Contract contract);

    void deleteContract(Long id);

    Contract getContractById(Long id);

    Contract getByContractNo(String contractNo);

    Page<Contract> searchContracts(
            String contractNo,
            Long houseId,
            Long landlordId,
            Long tenantId,
            ContractStatus status,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            Pageable pageable
    );

    // 模板数据：返回签约所需基本字段（房源、房东、租客信息通过各自接口前端拼装）
}

