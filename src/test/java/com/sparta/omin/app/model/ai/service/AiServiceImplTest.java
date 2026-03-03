package com.sparta.omin.app.model.ai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sparta.omin.app.model.ai.repos.AiLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * 프롬프트 생성 확인 및 전달 과정 확인 테스트 (API 호출 x)
 */
@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    private AiServiceImpl aiService;

    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec responseSpec;
    @Mock
    private AiLogRepository aiLogRepository;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl(chatClient, aiLogRepository);
    }

    @Test
    @DisplayName("프롬프트 생성 및 API 호출 흐름 검증")
    void generateMenuDescriptionTest() {
        // 1. Given: UserPrompt 생성
        String userPrompt = "가게 이름은 '스파르타' 만두, '서울 강남'에 있어. '할랄만두'를 판매하는데, '초등학생'이 좋아할만한 만두 상품의 이름과 상품설명을 작성해줘. 답변을 최대한 간결하게 50자 이하로.";

        String expectedContent = "트러플의 진한 풍미가 일품인 파스타로 소중한 기념일을 더 특별하게 만드세요.";

        // ChatClient의 체이닝 메소드 Mocking
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(expectedContent);

        // 2. When
        String result = aiService.generateMenuDescription(userPrompt);

        // 3. Then
        // 결과값 검증
        assertEquals(expectedContent, result);

        // 프롬프트 구성 검증을 위한 캡처
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatClient).prompt(promptCaptor.capture());

        String finalPrompt = promptCaptor.getValue().getContents();

        // 필드 포함 여부 확인
        assertTrue(finalPrompt.contains(userPrompt), "사용자 요청사항이 포함되어야 합니다.");

        System.out.println("생성된 프롬프트 검증 완료:\n" + finalPrompt);
    }
}