package com.sparta.omin.app.model.review.service;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.review.dto.ReviewCreateRequest;
import com.sparta.omin.app.model.review.dto.ReviewCriteria;
import com.sparta.omin.app.model.review.dto.ReviewResponse;
import com.sparta.omin.app.model.review.dto.ReviewUpdateRequest;
import com.sparta.omin.app.model.review.entity.Review;
import com.sparta.omin.app.model.review.entity.ReviewImage;
import com.sparta.omin.app.model.review.repos.ReviewRepository;
import com.sparta.omin.app.model.stats.entity.StoreRatingStat;
import com.sparta.omin.app.model.stats.repos.StoreRatingStatRepository;
import com.sparta.omin.app.model.user.constants.Role;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import com.sparta.omin.common.util.ImageUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final StoreRatingStatRepository statRepository;
    private final OrderRepository orderRepository;
    private final ImageUploader imageUploader;

    @Transactional
    public ReviewResponse createReview(User user, ReviewCreateRequest request, List<MultipartFile> images) {
        UUID loginUserId = user.getId();
        // 이미지 개수 초과 예외
        if (images != null && images.size() > 5) throw new OminBusinessException(ErrorCode.REVIEW_IMAGE_COUNT_EXCEEDED);
        // 사용자가 요청한 주문 조회
        Order order = orderRepository.findById(request.orderId()).orElseThrow(() -> new OminBusinessException(ErrorCode.ORDER_NOT_FOUND));
        // 로그인된 사용자 자신의 주문이 아니라면 예외
        if (!loginUserId.equals(order.getUser().getId()))
            throw new OminBusinessException(ErrorCode.ORDER_USER_MISMATCH);
        // 사용자 자신의 가게라면 예외
        if (user.getRole() == Role.OWNER && order.getStore().getOwnerId().equals(loginUserId)) {
            throw new OminBusinessException(ErrorCode.SELF_REVIEW_NOT_ALLOWED);
        }
        // 주문 상태 COMPLETED 아니면 예외
        if (!order.isCompleted()) throw new OminBusinessException(ErrorCode.ORDER_NOT_COMPLETED);

        // 주문일 + 2일 초과면 예외 --> status 변경시간 + 2일
        if (order.getCreatedAt().plusDays(2).isBefore(LocalDateTime.now())) {
            throw new OminBusinessException(ErrorCode.REVIEW_PERIOD_EXPIRED);
        }
        // 이미 리뷰 작성했으면 예외
        if (reviewRepository.existsByOrder_IdAndIsDeletedFalse(order.getId()))
            throw new OminBusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);

        // Review 생성
        Review newReview = Review.create(user, order, order.getStore(), request.rating(), request.comment());
        // 이미지 처리 (이미지가 있을 경우에만)
        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = images.stream().filter(file -> !file.isEmpty()).map(imageUploader::uploadReviewImage).toList();

            newReview.addImages(uploadedUrls); // 순서대로 들어간 리스트 전달
        }
        reviewRepository.save(newReview);

        // 주문에 해당하는 가게에 기존 평점 통계가 존재하는지 확인 후 평점 생성 / 업데이트
        Optional<StoreRatingStat> oldStoreRatingStat = statRepository.findByStoreId(order.getStore().getId());
        if (oldStoreRatingStat.isPresent()) {
            oldStoreRatingStat.get().increase(request.rating());
        } else {
            statRepository.save(StoreRatingStat.create(order.getStore().getId(), request.rating()));
        }
        return ReviewResponse.from(newReview);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReview(UUID reviewId) {
        Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId).orElseThrow(() -> new OminBusinessException(ErrorCode.REVIEW_NOT_FOUND));
        return ReviewResponse.from(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviews(ReviewCriteria criteria, Pageable pageable, UUID storeId) {
        if (!criteria.equals(ReviewCriteria.DEFAULT)) {
            // Criteria에 따른 정렬기준 하나 생성
            Sort.Order primaryOrder = switch (criteria) {
                case RATING_HIGH -> Sort.Order.desc("rating");
                case RATING_LOW -> Sort.Order.asc("rating");
                default -> Sort.Order.desc("createdAt");
            };

            // Criteria를 우선으로 새로운 Pageable에 쓰일 Sort을 만든다.
            // (pageable.getSort()의 기본값은 컨트롤러에서 @PageableDefault로 초기화되어있음)
            Sort combinedSort = Sort.by(primaryOrder).and(pageable.getSort());

            // 구현체 PageRequest로 combinedSort를 가진 새로운 Pagable생성
            Pageable finalPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), combinedSort);
            Page<Review> reviewPage = (storeId != null) ? reviewRepository.findAllByStoreIdAndIsDeletedFalse(storeId, finalPageable) : reviewRepository.findAllByIsDeletedFalse(finalPageable);
            return reviewPage.map(ReviewResponse::from);
        }//if criteria != DEFAULT

        else { // 만약 criteria가 요청에 제공되지 않았을 경우
            Page<Review> reviewPage = (storeId != null) ? reviewRepository.findAllByStoreIdAndIsDeletedFalse(storeId, pageable) : reviewRepository.findAllByIsDeletedFalse(pageable);
            return reviewPage.map(ReviewResponse::from);
        }
    }

    @Transactional
    public ReviewResponse updateReview(UUID reviewId, User user, ReviewUpdateRequest request, List<MultipartFile> images) {
        // 리뷰 조회
        Review old = reviewRepository.findByIdAndIsDeletedFalse(reviewId).orElseThrow(() -> new OminBusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인의 리뷰가 아니면 예외
        if (!old.getUser().getId().equals(user.getId())) {
            throw new OminBusinessException(ErrorCode.REVIEW_USER_MISMATCH);
        }

        // 주문일 + 2일 초과면 예외 --> status 변경시간 + 2일
        if (old.getOrder().getCreatedAt().plusDays(2).isBefore(LocalDateTime.now())) {
            throw new OminBusinessException(ErrorCode.REVIEW_UPDATE_PERIOD_EXPIRED);
        }
        if (request == null) {
            if (images.size() > 5) throw new OminBusinessException(ErrorCode.REVIEW_IMAGE_COUNT_EXCEEDED);
            handleReviewImageAdd(old, images); // request 없이 이미지만 추가
        } else {
            if (images != null && request.deleteImageUrls() != null && images.size() - request.deleteImageUrls().size() > 5) {
                throw new OminBusinessException(ErrorCode.REVIEW_IMAGE_COUNT_EXCEEDED);
            }

            // 통계 업데이트 (일단 비관적 락 사용)
            if (request.rating() != null && !request.rating().equals(old.getRating())) {
                double ratingDiff = request.rating() - old.getRating(); // 기존 평점과의 차이(양수: 올라감, 음수: 내려감)
                StoreRatingStat stat = statRepository.findByStoreIdWithLock(old.getStore().getId()).orElseGet(() -> statRepository.save(StoreRatingStat.create(old.getStore().getId(), 0)));
                stat.updateRatingByDiff(ratingDiff);
            }

            old.updateReview(request.rating() != null ? request.rating() : old.getRating(), request.comment() != null ? request.comment() : old.getComment());
            handleReviewImageUpdates(old, request.updateImages(), request.deleteImageUrls(), images);
        } // request != null (아래 부터 request가 Nullable)
        reviewRepository.saveAndFlush(old);// saveAndFlush를 하면 이 시점에 updatedAt이 세팅됩니다.
        return ReviewResponse.from(old);
    }

// TODO: 이미지 처리 로직
    private void handleReviewImageAdd(Review old, List<MultipartFile> newFiles) {
        boolean isUpdated = false; // 연관 엔티티 수정 시 updatedAt 갱신여부 flag
        // 새 이미지 추가
        if (newFiles != null && !newFiles.isEmpty()) {
            if (old.getImages().size() + newFiles.size() > 5) {
                throw new OminBusinessException(ErrorCode.REVIEW_IMAGE_COUNT_EXCEEDED);
            }

            int lastSequence = old.getImages().stream().mapToInt(ReviewImage::getSequence).max().orElse(-1);

            for (int i = 0; i < newFiles.size(); i++) {
                MultipartFile file = newFiles.get(i);
                if (!file.isEmpty()) {
                    String url = imageUploader.uploadReviewImage(file);
                    ReviewImage.create(old, url, lastSequence + i + 1);
                    isUpdated = true;
                }
            }
            if (isUpdated) {
                old.markUpdated();
            }
        }
    }

    private void handleReviewImageUpdates(Review review, List<ReviewImage> updateImages, // 클라이언트가 원하는 순서대로 바꾼 ReviewImage
                                          List<String> deleteUrls, // 클라이언트가 삭제하길 원하는 이미지 url
                                          List<MultipartFile> newFiles // 클라이언트가 추가하길 원하는 이미지 파일
    ) {
        boolean isUpdated = false; // 연관 엔티티 수정 시 updatedAt 갱신여부 flag
                        int deleteCount = deleteUrls == null ? 0 : deleteUrls.size();
        // 1️⃣ 소프트 삭제
        if (deleteUrls != null && !deleteUrls.isEmpty()) {
            for (String url : deleteUrls) {
                // isDeleted 상태 변경
                review.getImages().stream()
                        .filter(img -> img.getImageUrl().equals(url))
                        .findFirst()
                        .ifPresent(ReviewImage::delete);
                isUpdated = true;
            }
        }
        // 영속성 컨텍스트 반영 (메모리)
        if (deleteUrls != null && !deleteUrls.isEmpty()) {
            review.getImages().removeIf(ReviewImage::isDeleted);
        }

        // 2️⃣ 기존 이미지 순서 재배치
        if (updateImages != null && !updateImages.isEmpty()) {

            Map<String, ReviewImage> imageMap = review.getImages().stream().collect(Collectors.toMap(ReviewImage::getImageUrl, img -> img));

            for (ReviewImage dto : updateImages) {

                ReviewImage entity = imageMap.get(dto.getImageUrl());
                if (entity == null) continue;

                int newSeq = dto.getSequence();
                int oldSeq = entity.getSequence();

                entity.reorder(newSeq);

                if (oldSeq != newSeq) {
                    isUpdated = true;
                }
            }
        }

        // 3️⃣ 새 이미지 추가
        if (newFiles != null && !newFiles.isEmpty()) {
            if (review.getImages().size() - deleteCount + newFiles.size() > 5) {
                throw new OminBusinessException(ErrorCode.REVIEW_IMAGE_COUNT_EXCEEDED);
            }

            int lastSequence = review.getImages().stream().mapToInt(ReviewImage::getSequence).max().orElse(-1);

            for (int i = 0; i < newFiles.size(); i++) {
                MultipartFile file = newFiles.get(i);
                if (!file.isEmpty()) {
                    String url = imageUploader.uploadReviewImage(file);
                    ReviewImage.create(review, url, lastSequence + i + 1);
                    isUpdated = true;
                }
            }
        }

        // 4️⃣ 업데이트 발생 시 리뷰 updatedAt 갱신
        if (isUpdated) {
            review.markUpdated();
        }
    }
}
