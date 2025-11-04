package com.houseleasing.houseleasingmanagementsystem.model;

import com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rent_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;       // 关联合同（必须关联）

    private LocalDate dueDate;       // 应付日期

    private Double amount;           // 租金金额

    private LocalDateTime paidAt;    // 实付时间

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;    // 支付状态(待付、已付、逾期)
}

