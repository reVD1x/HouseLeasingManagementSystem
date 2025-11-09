package com.houseleasing.houseleasingmanagementsystem.model;

import com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Contract contract;       // 关联合同（必须关联）

    private LocalDate dueDate;       // 应付日期

    private Double amount;           // 租金金额

    private LocalDateTime paidAt;    // 实付时间

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;    // 支付状态(待付、已付、逾期)

    // 计费周期（可选）
    private LocalDate periodStart;   // 计费周期开始
    private LocalDate periodEnd;     // 计费周期结束

    // 提醒与逾期处理
    private LocalDateTime remindedAt;    // 上次提醒时间
    private Boolean overdueProcessed;    // 逾期是否已处理
    private Double penalty;              // 逾期罚金（可选）

    // 仅用于序列化输出的便捷字段
    @Transient
    @JsonProperty("contractId")
    public Long getContractId() { return (contract != null && Hibernate.isInitialized(contract)) ? contract.getId() : null; }

    @Transient
    @JsonProperty("contractNo")
    public String getContractNo() { return (contract != null && Hibernate.isInitialized(contract)) ? contract.getContractNo() : null; }

    @Transient
    @JsonProperty("tenantName")
    public String getTenantName() {
        if (contract == null || !Hibernate.isInitialized(contract)) return null;
        return (contract.getTenant() != null && Hibernate.isInitialized(contract.getTenant())) ? contract.getTenant().getRealName() : null;
    }

    @Transient
    @JsonProperty("landlordName")
    public String getLandlordName() {
        if (contract == null || !Hibernate.isInitialized(contract)) return null;
        return (contract.getLandlord() != null && Hibernate.isInitialized(contract.getLandlord())) ? contract.getLandlord().getRealName() : null;
    }

    @Transient
    @JsonProperty("houseAddress")
    public String getHouseAddress() {
        if (contract == null || !Hibernate.isInitialized(contract)) return null;
        return (contract.getHouse() != null && Hibernate.isInitialized(contract.getHouse())) ? contract.getHouse().getAddress() : null;
    }
}
