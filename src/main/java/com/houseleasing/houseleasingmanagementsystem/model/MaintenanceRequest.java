package com.houseleasing.houseleasingmanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.houseleasing.houseleasingmanagementsystem.model.enums.MaintenanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 允许请求写入，响应不直接输出
    private House house;             // 房源


    @Column(length = 1000)
    private String description;      // 问题描述

    private Double cost;             // 维修费用

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MaintenanceStatus status; // 状态(待处理、处理中、已完成)

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime completedAt; // 完成时间

    // 仅用于序列化输出的便捷字段
    @Transient
    @JsonProperty("houseId")
    public Long getHouseId() {
        return (house != null && Hibernate.isInitialized(house)) ? house.getId() : null;
    }

    @Transient
    @JsonProperty("houseAddress")
    public String getHouseAddress() {
        return (house != null && Hibernate.isInitialized(house)) ? house.getAddress() : null;
    }
}

