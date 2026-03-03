package com.sparta.omin.app.model.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

// TODO : 요청 프롬프트와 AI의 반환 결과를 저장할 DB 생성 및 저장 로직 구현
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;

    public AiServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String generateMenuDescription(String userPrompt) {

        String sysPrompt = """
            당신은 요식업 메뉴 전문 카피라이터입니다.
            
            [목표]
            아래 정보를 바탕으로 매력적인 메뉴 설명을 작성하세요.
            
            [작성 규칙]
            - 1~2문장으로 작성합니다.
            - 150자 이내로 작성합니다.
            - 타겟 고객이 매력을 느낄 표현을 포함합니다.
            - 다른 설명 없이 결과 문장만 출력합니다.
            """;

        SystemMessage systemMessage = SystemMessage.builder()
            .text(sysPrompt)
            .build();

        UserMessage userMessage = UserMessage.builder()
            .text(userPrompt)
            .build();

        Prompt prompt = Prompt.builder()
            .messages(systemMessage, userMessage)
            .chatOptions(ChatOptions.builder()
                .model("gpt-5-nano")
                .maxTokens(256)
                .temperature(0.7)
                .build())
            .build();

        return chatClient.prompt(prompt).call().content();
    }
}
