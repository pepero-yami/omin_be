package com.sparta.omin.app.model.cart.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.omin.app.model.cart.entity.RCart;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartRedisClient {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public <T> T get(UUID key, Class<T> classType) {
		return get(key.toString(), classType);
	}
	private <T> T get(String key, Class<T> classType) {
		Object redisValue = redisTemplate.opsForValue().get(key);
		if (ObjectUtils.isEmpty(redisValue)) {
			return null;
		} else {
			try {
				return objectMapper.readValue(redisValue.toString(), classType);
			} catch (JsonProcessingException e) {
				log.error("JsonMappingException(ParsingError): {}", e.getMessage());
				return null;
			}
		}
	}

	public void put(UUID key, RCart cart) {
		put(key.toString(), cart);
	}

	private void put(String key, RCart cart) {
		try {
			redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(cart));
		} catch (JsonProcessingException e) {
			throw new ApiException(ErrorCode.CART_CHANGE_FAIL);
		}
	}

}
