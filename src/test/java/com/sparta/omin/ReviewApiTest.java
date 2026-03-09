package com.sparta.omin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.omin.app.controller.review.ReviewController;
import com.sparta.omin.app.model.review.dto.ReviewCreateRequest;
import com.sparta.omin.app.model.review.dto.ReviewResponse;
import com.sparta.omin.app.model.review.dto.ReviewUpdateRequest;
import com.sparta.omin.app.model.review.service.ReviewService;
import com.sparta.omin.app.model.user.service.UserDetailsServiceImpl;
import com.sparta.omin.app.security.config.SecurityConfig;
import com.sparta.omin.app.security.jwt.JwtUtil;
import com.sparta.omin.common.error.GlobalExceptionHandler;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ReviewController.class)
@WithMockUser(username = "user@email.com")
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ReviewControllerTest {

    private final String BASE_URL = "/api/v1/reviews";
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    ReviewService reviewService;
    @MockitoBean
    JwtUtil jwtUtil;
    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    // given
    UUID reviewId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String nickName = "오민 최고";

    Double reqRating = 5.0;
    String reqComment = "맛있어요!";

    private MockMultipartFile createJsonPart(Object dto) throws Exception {
        return new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(dto)
        );
    }

    private List<MockMultipartFile> createImageParts(int count) {
        List<MockMultipartFile> parts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            parts.add(new MockMultipartFile(
                    "images",
                    "image" + i + ".jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    ("content" + i).getBytes()
            ));
        }
        return parts;
    }

    @Test
    @DisplayName("리뷰 생성 성공 시 201 반환")
    void createReview_success() throws Exception {

        ReviewCreateRequest request =
                new ReviewCreateRequest(orderId, reqRating, reqComment);

        // JSON 파트 생성
        MockMultipartFile requestPart = createJsonPart(request);

        // 이미지 파트 생성 (헬퍼 메서드 활용)
        List<MockMultipartFile> imageParts = createImageParts(2);

        ReviewResponse response =
                new ReviewResponse(
                        reviewId,
                        orderId,
                        userId,
                        nickName,
                        reqRating,
                        reqComment,
                        List.of("http://image.url"),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

        given(reviewService.createReview(any(), any(), any()))
                .willReturn(response);

        // when & then
        var requestBuilder = multipart(BASE_URL)
                .file(requestPart); // JSON 파트 추가

        for (MockMultipartFile image : imageParts) {
            requestBuilder.file(image); // 이미지 파트들 추가
        }

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.rating").value(reqRating))
                .andExpect(jsonPath("$.comment").value(reqComment))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.images[0]").value("http://image.url"));
    }

    @Test
    @DisplayName("요청 값 검증 실패 시 400 반환")
    void createReview_validationFail() throws Exception {
        // rating이 @Min, @Max 등으로 검증된다
        ReviewCreateRequest invalidRequest = new ReviewCreateRequest(null, 10.0, "");

        // 2. multipart()와 file()을 사용해서 전송
        mockMvc.perform(multipart(BASE_URL)
                        .file(createJsonPart(invalidRequest))) // 헬퍼 메서드 활용
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("비즈니스 예외 발생 시 409 반환")
    void createReview_conflict() throws Exception {
        UUID orderId = UUID.randomUUID();

        ReviewCreateRequest request =
                new ReviewCreateRequest(orderId, reqRating, reqComment);

        given(reviewService.createReview(any(), any(), any()))
                .willThrow(new OminBusinessException(ErrorCode.REVIEW_ALREADY_EXISTS));

        mockMvc.perform(multipart(BASE_URL)
                        .file(createJsonPart(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("REVIEW_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("리뷰 단건 조회 성공시 200 반환")
    void getReview_success() throws Exception {

        ReviewResponse response =
                new ReviewResponse(
                        reviewId,
                        orderId,
                        userId,
                        nickName,
                        reqRating,
                        reqComment,
                        List.of("http://image.url"),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

        given(reviewService.getReview(reviewId))
                .willReturn(response);

        mockMvc.perform(get(BASE_URL + "/" + reviewId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.rating").value(reqRating))
                .andExpect(jsonPath("$.comment").value(reqComment))
                .andExpect(jsonPath("$.images[0]").value("http://image.url"));
    }

    @Test
    @DisplayName("리뷰 목록 조회 성공시 200 반환")
    void getReviews_success() throws Exception {

        // given
        ReviewResponse review1 = new ReviewResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                userId,
                "리뷰어",
                5.0,
                "JMT!!!",
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        ReviewResponse review2 = new ReviewResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                userId,
                "리뷰어",
                4.5,
                "맛있다!!!",
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        List<ReviewResponse> content = List.of(review1, review2);

        Page<ReviewResponse> page =
                new PageImpl<>(content, PageRequest.of(0, 10), content.size());

        given(reviewService.getReviews(any(), any(), any()))
                .willReturn(page);

        // when & then
        mockMvc.perform(get(BASE_URL)
                        .param("criteria", "RATING_HIGH")
                        .param("sort", "rating,asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rating").value(5.0))
                .andExpect(jsonPath("$.content[1].rating").value(4.5))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("storeId로 리뷰 목록 조회 성공시 200 반환")
    void getReviews_byStoreId() throws Exception {

        UUID storeId = UUID.randomUUID();

        Page<ReviewResponse> page =
                new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        given(reviewService.getReviews(any(), any(), any()))
                .willReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("storeId", storeId.toString()))
                .andExpect(status().isOk());

        verify(reviewService)
                .getReviews(any(), any(), eq(storeId));
    }

    @Test
    @DisplayName("criteria enum 값이 잘못되면 400")
    void getReviews_invalidCriteria() throws Exception {

        mockMvc.perform(get(BASE_URL)
                        .param("criteria", "WRONG"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("criteria 없으면 DEFAULT 정렬로 조회")
    void getReviews_withoutCriteria() throws Exception {

        mockMvc.perform(get(BASE_URL))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리뷰 수정 성공 시 200 반환")
    void updateReview_success() throws Exception {

        ReviewUpdateRequest request =
                new ReviewUpdateRequest(
                        4.5,
                        "수정된 리뷰",
                        null,
                        List.of()
                );

        MockMultipartFile requestPart = createJsonPart(request);
        List<MockMultipartFile> imageParts = createImageParts(1);

        ReviewResponse response =
                new ReviewResponse(
                        reviewId,
                        orderId,
                        userId,
                        nickName,
                        4.5,
                        "수정된 리뷰",
                        List.of("http://image.url"),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

        given(reviewService.updateReview(any(), any(), any(), any()))
                .willReturn(response);

        MockMultipartHttpServletRequestBuilder requestBuilder =
                (MockMultipartHttpServletRequestBuilder) multipart(BASE_URL + "/" + reviewId)
                        .file(requestPart)
                        .with(req -> {
                            req.setMethod("PATCH");
                            return req;
                        });

        for (MockMultipartFile image : imageParts) {
            requestBuilder.file(image);
        }

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.comment").value("수정된 리뷰"));
    }

    @Test
    @DisplayName("리뷰 수정 validation 실패 시 400 반환")
    void updateReview_validationFail() throws Exception {

        ReviewUpdateRequest request =
                new ReviewUpdateRequest(
                        10.0, // invalid
                        "잘못된 평점",
                        null,
                        List.of()
                );

        MockMultipartFile requestPart = createJsonPart(request);

        mockMvc.perform(
                        multipart(BASE_URL + "/" + reviewId)
                                .file(requestPart)
                                .with(req -> {
                                    req.setMethod("PATCH");
                                    return req;
                                })
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test

    @DisplayName("리뷰 삭제 성공 시 200 반환")
    void deleteReview_success() throws Exception {

        // when & then
        mockMvc.perform(delete(BASE_URL + "/" + reviewId))
                .andDo(print())
                .andExpect(status().isOk());
        verify(reviewService).deleteReview(eq(reviewId), any());

    }


    @Test

    @DisplayName("리뷰 삭제 실패 - 작성자 불일치 시 403 반환")
    void deleteReview_fail_userMismatch() throws Exception {
        // void 메서드: doThrow()
        doThrow(new OminBusinessException(ErrorCode.REVIEW_USER_MISMATCH))
                .when(reviewService).deleteReview(eq(reviewId), any());

        // when & then
        mockMvc.perform(delete(BASE_URL + "/" + reviewId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("REVIEW_USER_MISMATCH"));

    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 존재하지 않는 리뷰일 경우 404 반환")
    void deleteReview_fail_notFound() throws Exception {

        // given
        doThrow(new OminBusinessException(ErrorCode.REVIEW_NOT_FOUND))
                .when(reviewService).deleteReview(eq(reviewId), any());

        // when & then

        mockMvc.perform(delete(BASE_URL + "/" + reviewId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("REVIEW_NOT_FOUND"));

    }
}