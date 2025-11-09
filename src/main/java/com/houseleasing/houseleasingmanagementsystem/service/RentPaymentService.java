package com.houseleasing.houseleasingmanagementsystem.service;

import com.houseleasing.houseleasingmanagementsystem.model.Contract;
import com.houseleasing.houseleasingmanagementsystem.model.RentPayment;
import com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface RentPaymentService {
    RentPayment create(RentPayment rentPayment);
    RentPayment update(Long id, RentPayment rentPayment);
    void delete(Long id);
    RentPayment getById(Long id);

    Page<RentPayment> search(Long contractId,
                             PaymentStatus status,
                             LocalDate dueFrom,
                             LocalDate dueTo,
                             Pageable pageable);

    // 报表：按日期范围统计
    List<RentPayment> listByDueDateBetween(LocalDate from, LocalDate to);

    // 提醒：找出即将到期或已逾期未付记录
    List<RentPayment> listOverduePending(LocalDate beforeDate);

    // 标记已提醒/逾期处理
    RentPayment markReminded(Long id);
    RentPayment markOverdueProcessed(Long id, Double penalty);

    // 标记支付
    RentPayment markPaid(Long id);

    // 根据合同生成整个租金计划；若 markFirstPaid=true 则第一笔标记为已支付
    void generateScheduleForContract(Contract contract, boolean markFirstPaid);
}
