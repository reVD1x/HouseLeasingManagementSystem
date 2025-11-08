package com.houseleasing.houseleasingmanagementsystem.repository;

import com.houseleasing.houseleasingmanagementsystem.model.Contract;
import com.houseleasing.houseleasingmanagementsystem.model.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {
    Page<Contract> findAllByStatus(ContractStatus status, Pageable pageable);

    Contract findByContractNo(String contractNo);

    @Query("select c from Contract c " +
            "left join fetch c.house h " +
            "left join fetch c.landlord l " +
            "left join fetch c.tenant t " +
            "where c.id = :id")
    Optional<Contract> findByIdWithRelations(@Param("id") Long id);

    @Query("select c from Contract c " +
            "join fetch c.tenant t " +
            "where c.house.id = :houseId " +
            "and c.status = 'ACTIVE' " +
            "and CURRENT_DATE between c.startDate and c.endDate " +
            "order by c.startDate desc")
    Optional<Contract> findActiveContractByHouseId(@Param("houseId") Long houseId);
}
