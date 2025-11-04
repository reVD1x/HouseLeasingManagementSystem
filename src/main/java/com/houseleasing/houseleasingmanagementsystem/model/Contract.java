package com.houseleasing.houseleasingmanagementsystem.model;

import com.houseleasing.houseleasingmanagementsystem.model.enums.ContractStatus;
import com.houseleasing.houseleasingmanagementsystem.model.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house;             // 房源

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private User tenant;             // 租客

    @ManyToOne
    @JoinColumn(name = "landlord_id")
    private User landlord;           // 房东

    @Column(nullable = false)
    private LocalDate startDate;     // 合同开始日期

    @Column(nullable = false)
    private LocalDate endDate;       // 合同结束日期

    @Column(nullable = false)
    private Double rentAmount;       // 租金金额

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // 支付方式

    @Column(length = 2000)
    private String breachClause;     // 违约条款

    @Enumerated(EnumType.STRING)
    private ContractStatus status;   // 合同状态

    @CreatedDate
    private LocalDateTime signedAt;  // 签订时间
}

