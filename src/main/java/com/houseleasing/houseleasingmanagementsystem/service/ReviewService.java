package com.houseleasing.houseleasingmanagementsystem.service;

import com.houseleasing.houseleasingmanagementsystem.model.Review;
import com.houseleasing.houseleasingmanagementsystem.model.enums.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {

    /**
     * 创建评价
     */
    Review createReview(Review review);

    /**
     * 根据ID获取评价
     */
    Review getReviewById(Long id);

    /**
     * 更新评价
     */
    Review updateReview(Long id, Review review);

    /**
     * 删除评价
     */
    void deleteReview(Long id);

    /**
     * 分页获取所有评价
     */
    Page<Review> getAllReviews(Pageable pageable);

    /**
     * 根据合同ID分页查询评价
     */
    Page<Review> getReviewsByContractId(Long contractId, Pageable pageable);

    /**
     * 根据评价人ID分页查询评价
     */
    Page<Review> getReviewsByReviewerId(Long reviewerId, Pageable pageable);

    /**
     * 根据被评价人ID分页查询评价
     */
    Page<Review> getReviewsByRevieweeId(Long revieweeId, Pageable pageable);

    /**
     * 根据评价类型分页查询评价
     */
    Page<Review> getReviewsByType(ReviewType reviewType, Pageable pageable);

    /**
     * 查询特定用户的平均评分
     */
    Double getAverageRatingByRevieweeId(Long revieweeId);

    /**
     * 查询特定评分范围的评价
     */
    Page<Review> getReviewsByMinRating(Integer minRating, Pageable pageable);

    /**
     * 统计用户收到的评价数量
     */
    Long countReviewsByRevieweeId(Long revieweeId);

    /**
     * 检查用户是否已经对该合同进行过评价
     */
    boolean hasUserReviewedContract(Long contractId, Long reviewerId);

    /**
     * 查询合同的所有评价
     */
    List<Review> getReviewsByContract(Long contractId);
}

