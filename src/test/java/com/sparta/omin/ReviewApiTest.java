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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

    private List<MockMultipartFile> createImageParts(String name, int count) {
        List<MockMultipartFile> parts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            parts.add(new MockMultipartFile(
                    name,
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
        List<MockMultipartFile> imageParts = createImageParts("images", 2);

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
                .willThrow(new ApiException(ErrorCode.REVIEW_ALREADY_EXISTS));

        mockMvc.perform(multipart(BASE_URL)
                        .file(createJsonPart(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("REVIEW_ALREADY_EXISTS"));
    }

    // 403 테스트

}