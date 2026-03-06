package com.sparta.omin.app.model.ai.service;

import static org.junit.jupiter.api.Assertions.*;

import com.sparta.omin.app.model.ai.repos.AiLogRepository;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Tag("external")
@Timeout(value=60, unit = TimeUnit.SECONDS)
@EnabledIfEnvironmentVariable(named="OPENAI_API_KEY", matches=".+")
class AiServiceRealCallTest {

    @Autowired
    private AiService aiService;

    @MockitoBean
    private AiLogRepository aiLogRepository;

    @Test
    void generateMenuDescription() {
        String userPrompt = "가게 이름은 '힐링' 만두, '서울 강남'에 있어. '할랄만두'를 판매하는데, '초등학생'이 좋아할만한 만두 상품의 이름과 상품설명을 작성해줘. 답변을 최대한 간결하게 50자 이하로.";

        String result = aiService.generateMenuDescription(userPrompt, UUID.randomUUID());

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.length() <= 600);
        System.out.println("LLM response = " + result);
    }
}