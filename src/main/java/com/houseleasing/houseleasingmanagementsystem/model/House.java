package com.houseleasing.houseleasingmanagementsystem.model;

import com.houseleasing.houseleasingmanagementsystem.model.enums.HouseStatus;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Entity
@Table(name = "houses")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String address;          // 房屋地址

    private String houseType;        // 户型

    private Double area;             // 面积

    @Column(nullable = false)
    private Double rent;             // 租金

    private String decoration;       // 装修情况

    @Column(length = 1000)
    private String facilities;       // 配套设施

    @Enumerated(EnumType.STRING)
    private HouseStatus status;      // 状态(可租、已租)

    @Column(length = 500)
    private String description;      // 房源描述

    private Boolean recommended;     // 是否推荐

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id")
    @JsonIgnore // 避免序列化触发懒加载
    private User landlord;           // 房东

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 在不暴露整个landlord对象的情况下，提供可序列化的房东信息
    @Transient
    @JsonProperty("landlordId")
    public Long getLandlordId() {
        return landlord != null ? landlord.getId() : null;
    }

    @Transient
    @JsonProperty("landlordName")
    public String getLandlordName() {
        return landlord != null ? landlord.getRealName() : null;
    }
}
