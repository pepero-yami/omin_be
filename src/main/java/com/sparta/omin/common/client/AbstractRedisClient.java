package com.sparta.omin.common.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRedisClient<V> implements RedisClient<UUID, V> {

	protected final RedisTemplate<String, Object> redisTemplate;
	protected final ObjectMapper objectMapper;
	private final Class<V> clazz;

	protected abstract String getPrefix();

	protected abstract RuntimeException getException();

	protected abstract Long getExpireTime();

	protected abstract TimeUnit getExpireTimeUnit();

	private String generateKey(String key) {
		return getPrefix() + ":" + key;
	}

	@Override
	public Optional<V> get(UUID key) {
		String fullKey = generateKey(key.toString());
		Object value = redisTemplate.opsForValue().get(fullKey);
		if (ObjectUtils.isEmpty(value)) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(objectMapper.readValue(value.toString(), clazz));
		} catch (JsonProcessingException e) {
			log.error("JsonMappingException(ParsingError) - deserialization | key:{}, Error: {}",
				key, e.getMessage());
			return Optional.empty();
		}
	}

	@Override
	public void put(UUID key, V value) {
		try {
			String fullKey = generateKey(key.toString());
			String jsonValue = objectMapper.writeValueAsString(value);
			redisTemplate.opsForValue()
				.set(fullKey, jsonValue, getExpireTime(), getExpireTimeUnit());
		} catch (JsonProcessingException e) {
			log.error("JsonMappingException(ParsingError) - serialization | key:{}, Error: {}",
				key, e.getMessage());
			throw getException();
		}
	}
}
