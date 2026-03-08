package com.sparta.omin.app.model.ai.service;

import com.sparta.omin.app.model.ai.code.RequestType;
import com.sparta.omin.app.model.ai.entity.AiLog;
import com.sparta.omin.app.model.ai.repos.AiLogRepository;
import com.sparta.omin.app.model.user.service.UserReadService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiLogService {

    private final AiLogRepository aiLogRepository;
    private final UserReadService userReadService;

    /**
     * 사용자 프롬프트, AI 응답, 요청 종류, 사용자ID를 DB에 저장합니다.<br>
     * <i>"로그 저장 기능은 부가기능"</i> 이라는 전제 하에, 로그저장에 실패시, 로그를 남기고 넘어가도록 구현하였습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAiLog(String input, String output, RequestType requestType, UUID userId) {
        try{
            aiLogRepository.save(AiLog.builder()
                .input(input)
                .output(output)
                .requestType(requestType)
                .userId(userId)
                .build());
        } catch (Exception e){
            log.warn("[WARN] Failed to save AiLog. requestType={}", requestType);
        }

    }
}
