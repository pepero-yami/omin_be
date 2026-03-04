package com.sparta.omin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.omin.app.controller.review.ReviewController;
import com.sparta.omin.app.model.review.dto.ReviewCreateRequest;
import com.sparta.omin.app.model.review.dto.ReviewResponse;
import com.sparta.omin.app.model.review.service.ReviewService;
import com.sparta.omin.app.model.user.service.UserDetailsServiceImpl;
import com.sparta.omin.app.security.config.SecurityConfig;
import com.sparta.omin.app.security.jwt.JwtUtil;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.GlobalExceptionHandler;
import com.sparta.omin.common.error.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test

    @DisplayName("리뷰 생성 성공 시 201 반환")
    void createReview_success() throws Exception {

        ReviewCreateRequest request =
                new ReviewCreateRequest(orderId, reqRating, reqComment);

        ReviewResponse response =
                new ReviewResponse(
                        reviewId,
                        orderId,
                        userId,
                        nickName,
                        reqRating,
                        reqComment,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

        given(reviewService.createReview(any(), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("요청 값 검증 실패 시 400 반환")
    void createReview_validationFail() throws Exception {
        // rating이 @Min, @Max 등으로 검증된다
        String invalidJson = """
                {
                  "orderId": null,
                  "rating": 10,
                  "comment": ""
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("비즈니스 예외 발생 시 409 반환")
    void createReview_conflict() throws Exception {
        UUID orderId = UUID.randomUUID();

        ReviewCreateRequest request =
                new ReviewCreateRequest(orderId, reqRating, reqComment);

        given(reviewService.createReview(any(), any()))
                .willThrow(new ApiException(ErrorCode.REVIEW_ALREADY_EXISTS));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("이미 해당 주문 건으로 작성된 리뷰가 있습니다."));
    }

}