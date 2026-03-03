package com.sparta.omin.app.model.ai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sparta.omin.app.model.ai.code.RequestType;
import com.sparta.omin.app.model.ai.entity.AiLog;
import com.sparta.omin.app.model.ai.repos.AiLogRepository;
import com.sparta.omin.app.model.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiServiceImplUnitTest {

    @Mock
    private AiLogRepository aiLogRepository;

    @InjectMocks
    private AiServiceImpl aiLogService;

    @Test
    @DisplayName("save()호출 확인 및 AiLog 값 확인")
    void createAiLog_shouldSaveCorrectEntity() {
        // given
        String input = "사용자 질문";
        String output = "AI 응답";
        RequestType requestType = RequestType.PRODUCT_DESCRIPTION;
        User user = User.builder()
            .name("홍길동")
            .nickname("길동이")
            .email("gildong@test.com")
            .password("raw-password")
            .build();

        ArgumentCaptor<AiLog> captor = ArgumentCaptor.forClass(AiLog.class);

        // when
        aiLogService.createAiLog(input, output, requestType, user);

        // then
        verify(aiLogRepository, times(1)).save(captor.capture());

        AiLog savedLog = captor.getValue();

        assertEquals(input, savedLog.getInput());
        assertEquals(output, savedLog.getOutput());
        assertEquals(requestType, savedLog.getRequestType());
        assertSame(user, savedLog.getUser());
    }
}