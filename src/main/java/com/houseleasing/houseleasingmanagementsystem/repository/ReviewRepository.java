package com.houseleasing.houseleasingmanagementsystem.repository;

import com.houseleasing.houseleasingmanagementsystem.model.Review;
import com.houseleasing.houseleasingmanagementsystem.model.enums.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 查询所有评价（JOIN FETCH 避免 LazyInitializationException）
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.contract " +
           "LEFT JOIN FETCH r.reviewer " +
           "LEFT JOIN FETCH r.reviewee")
    List<Review> findAllWithRelations();

    // 分页查询所有评价（JOIN FETCH）
    @Query(value = "SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.contract " +
           "LEFT JOIN FETCH r.reviewer " +
           "LEFT JOIN FETCH r.reviewee",
           countQuery = "SELECT COUNT(r) FROM Review r")
    Page<Review> findAllWithRelations(Pageable pageable);

    // 根据合同ID查询评价
    Page<Review> findByContract_Id(Long contractId, Pageable pageable);

    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.contract c " +
           "LEFT JOIN FETCH r.reviewer " +
           "LEFT JOIN FETCH r.reviewee " +
           "WHERE c.id = :contractId")
    List<Review> findByContractIdWithRelations(@Param("contractId") Long contractId);

    // 根据评价人ID查询评价
    Page<Review> findByReviewer_Id(Long reviewerId, Pageable pageable);

    @Query(value = "SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.contract " +
           "LEFT JOIN FETCH r.reviewer rv " +
           "LEFT JOIN FETCH r.reviewee " +
           "WHERE rv.id = :reviewerId",
           countQuery = "SELECT COUNT(r) FROM Review r WHERE r.reviewer.id = :reviewerId")
    Page<Review> findByReviewerIdWithRelations(@Param("reviewerId") Long reviewerId, Pageable pageable);

    // 根据被评价人ID查询评价
    Page<Review> findByReviewee_Id(Long revieweeId, Pageable pageable);

    @Query(value = "SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.contract " +
           "LEFT JOIN FETCH r.reviewer " +
           "LEFT JOIN FETCH r.reviewee re " +
           "WHERE re.id = :revieweeId",
           countQuery = "SELECT COUNT(r) FROM Review r WHERE r.reviewee.id = :revieweeId")
    Page<Review> findByRevieweeIdWithRelations(@Param("revieweeId") Long revieweeId, Pageable pageable);

    // 根据评价类型查询评价
    Page<Review> findByReviewType(ReviewType reviewType, Pageable pageable);

    @Query(value = "SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.contract " +
           "LEFT JOIN FETCH r.reviewer " +
           "LEFT JOIN FETCH r.reviewee " +
           "WHERE r.reviewType = :reviewType",
           countQuery = "SELECT COUNT(r) FROM Review r WHERE r.reviewType = :reviewType")
    Page<Review> findByReviewTypeWithRelations(@Param("reviewType") ReviewType reviewType, Pageable pageable);

    // 根据ID查询单个评价（JOIN FETCH）
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.contract " +
           "LEFT JOIN FETCH r.reviewer " +
           "LEFT JOIN FETCH r.reviewee " +
           "WHERE r.id = :id")
    Optional<Review> findByIdWithRelations(@Param("id") Long id);

    // 查询特定合同和评价人的评价（防止重复评价）
    Optional<Review> findByContract_IdAndReviewer_Id(Long contractId, Long reviewerId);

    // 查询特定用户作为评价人的所有评价
    List<Review> findByReviewer_Id(Long reviewerId);

    // 查询特定用户收到的所有评价
    List<Review> findByReviewee_Id(Long revieweeId);

    // 查询特定用户的平均评分
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :revieweeId")
    Double findAverageRatingByRevieweeId(@Param("revieweeId") Long revieweeId);

    // 查询特定评分范围的评价
    Page<Review> findByRatingGreaterThanEqual(Integer rating, Pageable pageable);

    @Query(value = "SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.contract " +
           "LEFT JOIN FETCH r.reviewer " +
           "LEFT JOIN FETCH r.reviewee " +
           "WHERE r.rating >= :rating",
           countQuery = "SELECT COUNT(r) FROM Review r WHERE r.rating >= :rating")
    Page<Review> findByRatingGreaterThanEqualWithRelations(@Param("rating") Integer rating, Pageable pageable);

    // 统计特定用户收到的评价数量
    Long countByReviewee_Id(Long revieweeId);

    // 查询合同相关的所有评价
    List<Review> findByContract_Id(Long contractId);

    // 查询最新 5 条评价（用于统计面板）
    List<Review> findTop5ByOrderByCreatedAtDesc();
}
