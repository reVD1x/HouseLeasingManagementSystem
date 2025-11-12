package com.houseleasing.houseleasingmanagementsystem.repository;

import com.houseleasing.houseleasingmanagementsystem.model.RentPayment;
import com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentPaymentRepository extends JpaRepository<RentPayment, Long>, JpaSpecificationExecutor<RentPayment> {
    Page<RentPayment> findAllByStatus(PaymentStatus status, Pageable pageable);

    @Query("select rp from RentPayment rp join rp.contract c where c.id = :contractId")
    Page<RentPayment> findByContractId(@Param("contractId") Long contractId, Pageable pageable);

    // Use nested property path to check existence by associated contract's id
    boolean existsByContract_Id(Long contractId);

    // Check if a rent payment already exists for a contract on a specific due date
    boolean existsByContract_IdAndDueDate(Long contractId, java.time.LocalDate dueDate);

    List<RentPayment> findByDueDateBetween(LocalDate from, LocalDate to);

    List<RentPayment> findByStatusAndDueDateBefore(PaymentStatus status, LocalDate date);

    @Query("select rp from RentPayment rp " +
           "left join fetch rp.contract c " +
           "left join fetch c.house h " +
           "left join fetch c.landlord l " +
           "left join fetch c.tenant t " +
           "where rp.id = :id")
    Optional<RentPayment> findByIdWithRelations(@Param("id") Long id);

    @Query("select rp from RentPayment rp " +
           "left join fetch rp.contract c " +
           "left join fetch c.house h " +
           "left join fetch c.landlord l " +
           "left join fetch c.tenant t " +
           "where rp.dueDate < :date and rp.status in :statuses")
    List<RentPayment> findOverdueBefore(@Param("date") LocalDate date,
                                        @Param("statuses") List<com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentStatus> statuses);
}
