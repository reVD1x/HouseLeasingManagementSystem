package com.houseleasing.houseleasingmanagementsystem.model;

import com.houseleasing.houseleasingmanagementsystem.model.enums.MaintenanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house;             // 房源

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private User tenant;             // 申请人

    @Column(length = 1000)
    private String description;      // 问题描述

    private Double cost;             // 维修费用

    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status; // 状态(待处理、处理中、已完成)

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime completedAt; // 完成时间
}

