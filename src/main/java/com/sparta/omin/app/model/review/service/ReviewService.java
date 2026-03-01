package com.sparta.omin.app.model.review.service;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.review.dto.ReviewCreateRequest;
import com.sparta.omin.app.model.review.dto.ReviewResponse;
import com.sparta.omin.app.model.review.entity.Review;
import com.sparta.omin.app.model.review.repos.ReviewRepository;
import com.sparta.omin.app.model.stats.entity.StoreRatingStat;
import com.sparta.omin.app.model.stats.repos.StoreRatingStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final StoreRatingStatRepository statRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {

        // 1. 주문 조회
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalStateException("주문이 존재하지 않습니다."));


        // 2. 주문 상태 COMPLETED 아니면 예외
        if (!order.isCompleted()) {
            throw new IllegalStateException("주문 완료 후에만 리뷰 작성이 가능합니다.");
        }
        // 3. 주문일 + 2일 초과면 예외
        if (order.getCreatedAt().
                plusDays(2).
                isBefore(LocalDateTime.now())
        ) {
            throw new IllegalStateException("주문일로부터 2일이 초과되어 리뷰를 작성할 수 없습니다.");
        }

        // 4. 이미 리뷰 있으면 예외
        Optional<Review> oldReview = reviewRepository.findByOrderId(request.orderId());
        if (oldReview.isPresent()) {
            throw new IllegalStateException("이미 해당 주문 건으로 작성된 리뷰가 있습니다.");
        }

        // 5. Review 생성
        Review newReview = Review.create(
                request.userId(),
                request.orderId(),
                request.rating(),
                request.comment(),
                request.actorId()
        );
        reviewRepository.save(newReview);

        // 해당 가게에 평점이 존재하는지 확인
        Optional<StoreRatingStat> storeRatingStat = statRepository.findByStoreId((order.getStoreId()));

        if (storeRatingStat.isPresent()) {
            storeRatingStat.get().increase(request.rating());
        } else {
            statRepository.save(StoreRatingStat.create(order.getStoreId(), request.rating()));
        }

        return new ReviewResponse(
                newReview.getId(),
                order.getStoreId(),
                newReview.getRating(),
                newReview.getComment(),
                newReview.getCreatedAt()
        );
    }
}
