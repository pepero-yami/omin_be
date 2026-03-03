package com.sparta.omin.app.model.ai.service;

import com.sparta.omin.app.model.ai.code.RequestType;
import com.sparta.omin.app.model.ai.entity.AiLog;
import com.sparta.omin.app.model.ai.repos.AiLogRepository;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.error.constants.ErrorCode;
import com.sparta.omin.common.error.exceptions.CommonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;
    private final AiLogRepository aiLogRepository;

    @Override
    public String generateMenuDescription(String userPrompt) {

        // 입력 검증
        if(!StringUtils.hasText(userPrompt)) {
            throw new CommonException(ErrorCode.INVALID_AI_PROMPT);
        }

        String sysPrompt = """
            당신은 요식업 메뉴 전문 카피라이터입니다.
            
            [목표]
            아래 정보를 바탕으로 매력적인 메뉴 설명을 작성하세요.
            
            [작성 규칙]
            - 1~2문장으로 작성합니다.
            - 150자 이내로 작성합니다.
            - 타겟 고객이 매력을 느낄 표현을 포함합니다.
            - 다른 설명 없이 결과 문장만 출력합니다.
            
            [유저 요청사항]
            """;

        SystemMessage systemMessage = SystemMessage.builder()
            .text(sysPrompt)
            .build();

        UserMessage userMessage = UserMessage.builder()
            .text(userPrompt)
            .build();

        Prompt prompt = Prompt.builder()
            .messages(systemMessage, userMessage)
            .chatOptions(OpenAiChatOptions.builder()
                .model("gpt-5-nano")
                .temperature(1.0)
//                .maxCompletionTokens(25000)
                .build())
            .build();

        final String response;

        // 외부 호출 예외 처리
        try {
            response = chatClient.prompt(prompt).call().content();
        } catch (ResourceAccessException e) {
            log.error("[ERROR] Resource access failed (maybe timeout)", e);
            throw new CommonException(ErrorCode.AI_TIMEOUT, e);
        } catch (Exception e) {
            log.error("[ERROR] API call failed", e);
            throw new CommonException(ErrorCode.AI_GENERATION_FAILED, e);
        }

        // 빈 응답 처리
        if (!StringUtils.hasText(response)) {
            log.warn("[WARN] Received empty response from OpenAI");
            throw new CommonException(ErrorCode.AI_EMPTY_RESPONSE);
        }

        return response.trim();
    }

    /**
     * 사용자 프롬프트, AI 응답, 요청 종류, 사용자ID를 DB에 저장합니다.<br>
     * <i>"로그 저장 기능은 부가기능"</i> 이라는 전제 하에, 로그저장에 실패시, 로그를 남기고 넘어가도록 구현하였습니다.
     */
    @Override
    @Transactional
    public void createAiLog(String input, String output, RequestType requestType, User user) {
        try{
            aiLogRepository.save(AiLog.builder()
                .input(input)
                .output(output)
                .requestType(requestType)
                .user(user)
                .build());
        } catch (Exception e){
            log.warn("[WARN] Failed to save AiLog. requestType={}", requestType);
        }

    }
}
