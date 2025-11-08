package com.houseleasing.houseleasingmanagementsystem.controller;

import com.houseleasing.houseleasingmanagementsystem.dto.ReviewDTO;
import com.houseleasing.houseleasingmanagementsystem.model.Contract;
import com.houseleasing.houseleasingmanagementsystem.model.Review;
import com.houseleasing.houseleasingmanagementsystem.model.User;
import com.houseleasing.houseleasingmanagementsystem.model.enums.ReviewType;
import com.houseleasing.houseleasingmanagementsystem.repository.ContractRepository;
import com.houseleasing.houseleasingmanagementsystem.repository.UserRepository;
import com.houseleasing.houseleasingmanagementsystem.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private UserRepository userRepository;


    /**
     * DTO转实体
     */
    private Review convertToEntity(ReviewDTO dto) {
        Review review = new Review();
        review.setId(dto.getId());

        // 设置合同
        if (dto.getContractId() != null) {
            Contract contract = contractRepository.findById(dto.getContractId())
                    .orElseThrow(() -> new IllegalArgumentException("合同不存在"));
            review.setContract(contract);
        }

        // 设置评价人
        if (dto.getReviewerId() != null) {
            User reviewer = userRepository.findById(dto.getReviewerId())
                    .orElseThrow(() -> new IllegalArgumentException("评价人不存在"));
            review.setReviewer(reviewer);
        }

        // 设置被评价人
        if (dto.getRevieweeId() != null) {
            User reviewee = userRepository.findById(dto.getRevieweeId())
                    .orElseThrow(() -> new IllegalArgumentException("被评价人不存在"));
            review.setReviewee(reviewee);
        }

        review.setReviewType(dto.getReviewType());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        return review;
    }

    /**
     * 创建评价
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            if (reviewService.hasUserReviewedContract(reviewDTO.getContractId(), reviewDTO.getReviewerId())) {
                return ResponseEntity.badRequest().body("已评价过该合同");
            }
            Review review = convertToEntity(reviewDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(review));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取所有评价（分页）
     * GET /api/reviews?page=0&size=10&sort=createdAt,desc
     * 支持可选过滤参数：reviewerId, revieweeId, reviewType, contractId, minRating
     */
    @GetMapping
    public ResponseEntity<Page<Review>> getAllReviews(
            @RequestParam(required = false) Long reviewerId,
            @RequestParam(required = false) Long revieweeId,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) ReviewType reviewType,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        // 处理排序参数
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<Review> reviews;

        // 根据提供的参数进行过滤
        if (reviewerId != null) {
            reviews = reviewService.getReviewsByReviewerId(reviewerId, pageable);
        } else if (revieweeId != null) {
            reviews = reviewService.getReviewsByRevieweeId(revieweeId, pageable);
        } else if (contractId != null) {
            reviews = reviewService.getReviewsByContractId(contractId, pageable);
        } else if (reviewType != null) {
            reviews = reviewService.getReviewsByType(reviewType, pageable);
        } else if (minRating != null) {
            reviews = reviewService.getReviewsByMinRating(minRating, pageable);
        } else {
            reviews = reviewService.getAllReviews(pageable);
        }

        return ResponseEntity.ok(reviews);
    }

    /**
     * 根据ID获取评价
     * GET /api/reviews/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Review review = reviewService.getReviewById(id);
        return review != null ? ResponseEntity.ok(review) : ResponseEntity.notFound().build();
    }

    /**
     * 更新评价
     * PUT /api/reviews/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            Review review = convertToEntity(reviewDTO);
            Review updated = reviewService.updateReview(id, review);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除评价
     * DELETE /api/reviews/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据合同ID查询评价
     * GET /api/reviews/contract/{contractId}
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<Page<Review>> getReviewsByContract(
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.getReviewsByContractId(contractId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 查询合同的所有评价（不分页）
     * GET /api/reviews/contract/{contractId}/all
     */
    @GetMapping("/contract/{contractId}/all")
    public ResponseEntity<List<Review>> getAllReviewsByContract(@PathVariable Long contractId) {
        List<Review> reviews = reviewService.getReviewsByContract(contractId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 根据评价人ID查询其发出的评价
     * GET /api/reviews/reviewer/{reviewerId}
     */
    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<Page<Review>> getReviewsByReviewer(
            @PathVariable Long reviewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.getReviewsByReviewerId(reviewerId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 根据被评价人ID查询其收到的评价
     * GET /api/reviews/reviewee/{revieweeId}
     */
    @GetMapping("/reviewee/{revieweeId}")
    public ResponseEntity<Page<Review>> getReviewsByReviewee(
            @PathVariable Long revieweeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.getReviewsByRevieweeId(revieweeId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 根据评价类型查询评价
     * GET /api/reviews/type/{reviewType}
     */
    @GetMapping("/type/{reviewType}")
    public ResponseEntity<Page<Review>> getReviewsByType(
            @PathVariable ReviewType reviewType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.getReviewsByType(reviewType, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 获取用户的平均评分和评价统计
     * GET /api/reviews/stats/{userId}
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserReviewStats(@PathVariable Long userId) {
        Double avgRating = reviewService.getAverageRatingByRevieweeId(userId);
        Long reviewCount = reviewService.countReviewsByRevieweeId(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("averageRating", avgRating);
        stats.put("totalReviews", reviewCount);

        return ResponseEntity.ok(stats);
    }

    /**
     * 查询高评分评价
     * GET /api/reviews/high-rating?minRating=4
     */
    @GetMapping("/high-rating")
    public ResponseEntity<Page<Review>> getHighRatingReviews(
            @RequestParam(defaultValue = "4") Integer minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
        Page<Review> reviews = reviewService.getReviewsByMinRating(minRating, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 检查用户是否已对合同进行评价
     * GET /api/reviews/check?contractId={contractId}&reviewerId={reviewerId}
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkReviewExists(
            @RequestParam Long contractId,
            @RequestParam Long reviewerId) {
        boolean hasReviewed = reviewService.hasUserReviewedContract(contractId, reviewerId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("hasReviewed", hasReviewed);
        return ResponseEntity.ok(result);
    }
}

