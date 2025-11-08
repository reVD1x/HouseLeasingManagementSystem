package com.houseleasing.houseleasingmanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.houseleasing.houseleasingmanagementsystem.model.enums.ReviewType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Contract contract;       // 关联的合同

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User reviewer;           // 评价人

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User reviewee;           // 被评价人

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType reviewType;   // 评价类型

    @Column(nullable = false)
    private Integer rating;          // 评分 (1-5)

    @Column(length = 2000)
    private String comment;          // 评价内容

    @CreatedDate
    private LocalDateTime createdAt; // 评价时间

    // 用于序列化输出的便捷字段
    @Transient
    @JsonProperty("contractId")
    public Long getContractId() {
        return (contract != null && Hibernate.isInitialized(contract)) ? contract.getId() : null;
    }

    @Transient
    @JsonProperty("contractNo")
    public String getContractNo() {
        return (contract != null && Hibernate.isInitialized(contract)) ? contract.getContractNo() : null;
    }

    @Transient
    @JsonProperty("reviewerId")
    public Long getReviewerId() {
        return (reviewer != null && Hibernate.isInitialized(reviewer)) ? reviewer.getId() : null;
    }

    @Transient
    @JsonProperty("reviewerName")
    public String getReviewerName() {
        return (reviewer != null && Hibernate.isInitialized(reviewer)) ? reviewer.getRealName() : null;
    }

    @Transient
    @JsonProperty("revieweeId")
    public Long getRevieweeId() {
        return (reviewee != null && Hibernate.isInitialized(reviewee)) ? reviewee.getId() : null;
    }

    @Transient
    @JsonProperty("revieweeName")
    public String getRevieweeName() {
        return (reviewee != null && Hibernate.isInitialized(reviewee)) ? reviewee.getRealName() : null;
    }
}

