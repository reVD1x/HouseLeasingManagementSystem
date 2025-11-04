package com.houseleasing.houseleasingmanagementsystem.model;

import com.houseleasing.houseleasingmanagementsystem.model.enums.ReviewType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;       // 关联合同

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;           // 评价人

    private Integer rating;          // 评分(1-5)

    @Column(length = 1000)
    private String comment;          // 评价内容

    @Enumerated(EnumType.STRING)
    private ReviewType type;         // 评价类型(租客评价房东、房东评价租客)

    @CreatedDate
    private LocalDateTime createdAt;
}

