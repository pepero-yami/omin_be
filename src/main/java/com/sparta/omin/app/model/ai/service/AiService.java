package com.sparta.omin.app.model.ai.service;

import com.sparta.omin.app.model.ai.code.RequestType;
import com.sparta.omin.app.model.user.entity.User;

public interface AiService {
    String generateMenuDescription(String userPrompt);
    void createAiLog(String input, String output, RequestType requestType, User user);
}
