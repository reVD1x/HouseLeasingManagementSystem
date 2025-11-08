package com.houseleasing.houseleasingmanagementsystem.service.impl;

import com.houseleasing.houseleasingmanagementsystem.model.Review;
import com.houseleasing.houseleasingmanagementsystem.model.enums.ReviewType;
import com.houseleasing.houseleasingmanagementsystem.repository.ReviewRepository;
import com.houseleasing.houseleasingmanagementsystem.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public Review createReview(Review review) {
        // 验证评分范围
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
        return reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Review getReviewById(Long id) {
        return reviewRepository.findByIdWithRelations(id).orElse(null);
    }

    @Override
    public Review updateReview(Long id, Review review) {
        Review existingReview = reviewRepository.findByIdWithRelations(id).orElse(null);
        if (existingReview != null) {
            // 验证评分范围
            if (review.getRating() < 1 || review.getRating() > 5) {
                throw new IllegalArgumentException("评分必须在1-5之间");
            }
            existingReview.setRating(review.getRating());
            existingReview.setComment(review.getComment());
            return reviewRepository.save(existingReview);
        }
        return null;
    }

    @Override
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAllWithRelations(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByContractId(Long contractId, Pageable pageable) {
        return reviewRepository.findByContract_Id(contractId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByReviewerId(Long reviewerId, Pageable pageable) {
        return reviewRepository.findByReviewerIdWithRelations(reviewerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByRevieweeId(Long revieweeId, Pageable pageable) {
        return reviewRepository.findByRevieweeIdWithRelations(revieweeId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByType(ReviewType reviewType, Pageable pageable) {
        return reviewRepository.findByReviewTypeWithRelations(reviewType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByRevieweeId(Long revieweeId) {
        Double avgRating = reviewRepository.findAverageRatingByRevieweeId(revieweeId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByMinRating(Integer minRating, Pageable pageable) {
        return reviewRepository.findByRatingGreaterThanEqualWithRelations(minRating, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countReviewsByRevieweeId(Long revieweeId) {
        return reviewRepository.countByReviewee_Id(revieweeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedContract(Long contractId, Long reviewerId) {
        return reviewRepository.findByContract_IdAndReviewer_Id(contractId, reviewerId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByContract(Long contractId) {
        return reviewRepository.findByContract_Id(contractId);
    }
}

