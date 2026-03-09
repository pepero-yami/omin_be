package com.sparta.omin.app.model.user.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.omin.app.model.user.dto.response.TokenResponse;
import com.sparta.omin.common.client.AbstractRedisClient;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JwtRedisClient extends AbstractRedisClient<TokenResponse> {

	public JwtRedisClient(
		RedisTemplate<String, Object> redisTemplate,
		ObjectMapper objectMapper) {
		super(redisTemplate, objectMapper, TokenResponse.class);
	}

	@Override
	protected String getPrefix() {
		return "RT";
	}

	@Override
	protected RuntimeException getException() {
		return new OminBusinessException(ErrorCode.TOKEN_SAVE_FAILED);
	}

	@Override
	protected Long getExpireTime() {
		return 43_002_000L;
	}

	@Override
	protected TimeUnit getExpireTimeUnit() {
		return TimeUnit.MILLISECONDS;
	}
}
