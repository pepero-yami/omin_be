package com.sparta.omin.app.model.review.service;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.review.dto.ReviewCreateRequest;
import com.sparta.omin.app.model.review.dto.ReviewResponse;
import com.sparta.omin.app.model.review.entity.Review;
import com.sparta.omin.app.model.review.repos.ReviewRepository;
import com.sparta.omin.app.model.stats.entity.StoreRatingStat;
import com.sparta.omin.app.model.stats.repos.StoreRatingStatRepository;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import com.sparta.omin.common.util.ImageUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final StoreRatingStatRepository statRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ImageUploader imageUploader;

    @Transactional
    public ReviewResponse createReview(String email, ReviewCreateRequest request, List<MultipartFile> images) {
        // 이미지 개수 초과 예외
        if (images.size() > 5) throw new ApiException(ErrorCode.REVIEW_IMAGE_COUNT_EXCEEDED);
        // 주문 조회
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));
        // 주문 상태 COMPLETED 아니면 예외
        if (!order.isCompleted()) {
            throw new ApiException(ErrorCode.ORDER_NOT_COMPLETED);
        }
        // 주문일 + 2일 초과면 예외
        if (order.getCreatedAt().
                plusDays(2).
                isBefore(LocalDateTime.now())
        ) {
            throw new ApiException(ErrorCode.REVIEW_PERIOD_EXPIRED);
        }
        // 이미 리뷰 작성했으면 예외
        Optional<Review> oldReview = reviewRepository.findByOrderIdAndIsDeletedFalse(request.orderId());
        if (oldReview.isPresent()) {
            throw new ApiException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 요청에 해당하는 유저 정보 조회
        User user = userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        UUID userId = user.getId();

        // Review 생성
        Review newReview = Review.create(
                userId,
                request.orderId(),
                request.rating(),
                request.comment()
        );

        // 이미지 처리 (이미지가 있을 경우에만)
        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = images.stream()
                    .filter(file -> !file.isEmpty())
                    .map(imageUploader::uploadReviewImage)
                    .toList();

            newReview.addImages(uploadedUrls); // 순서대로 들어간 리스트 전달
        }
        reviewRepository.save(newReview);

        // 해당 가게에 기존 평점 통계가 존재하는지 확인 후 평점 생성 / 업데이트
        Optional<StoreRatingStat> oldStoreRatingStat = statRepository.findByStoreId((order.getStoreId()));
        if (oldStoreRatingStat.isPresent()) {
            oldStoreRatingStat.get().increase(request.rating());
        } else {
            statRepository.save(StoreRatingStat.create(order.getStoreId(), request.rating()));
        }
        // 단일 응답 생성
        String nickName = user.getNickname();
        return ReviewResponse.of(newReview, nickName);
    }

}
