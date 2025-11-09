package com.houseleasing.houseleasingmanagementsystem.model;

import com.houseleasing.houseleasingmanagementsystem.model.enums.ContractStatus;
import com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentMethod;
import com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentCycle;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String contractNo;       // 合同编号

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 允许请求写入，响应不直接输出
    private House house;             // 房源

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User tenant;             // 租客

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User landlord;           // 房东

    @Column(nullable = false)
    private LocalDate startDate;     // 合同开始日期

    @Column(nullable = false)
    private LocalDate endDate;       // 合同结束日期

    @Column(nullable = false)
    private Double rentAmount;       // 租金金额

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private PaymentCycle paymentCycle;   // 支付周期（频率）

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private PaymentMethod paymentMethod; // 支付方式（渠道）

    @Column(length = 2000)
    private String breachClause;     // 违约条款

    @Enumerated(EnumType.STRING)
    private ContractStatus status;   // 合同状态

    @CreatedDate
    private LocalDateTime signedAt;  // 签订时间

    // 仅用于序列化输出的便捷字段
    @Transient
    @JsonProperty("houseId")
    public Long getHouseId() { return (house != null && Hibernate.isInitialized(house)) ? house.getId() : null; }

    @Transient
    @JsonProperty("houseAddress")
    public String getHouseAddress() { return (house != null && Hibernate.isInitialized(house)) ? house.getAddress() : null; }

    @Transient
    @JsonProperty("tenantId")
    public Long getTenantId() { return (tenant != null && Hibernate.isInitialized(tenant)) ? tenant.getId() : null; }

    @Transient
    @JsonProperty("tenantName")
    public String getTenantName() { return (tenant != null && Hibernate.isInitialized(tenant)) ? tenant.getRealName() : null; }

    @Transient
    @JsonProperty("landlordId")
    public Long getLandlordId() { return (landlord != null && Hibernate.isInitialized(landlord)) ? landlord.getId() : null; }

    @Transient
    @JsonProperty("landlordName")
    public String getLandlordName() { return (landlord != null && Hibernate.isInitialized(landlord)) ? landlord.getRealName() : null; }
}
