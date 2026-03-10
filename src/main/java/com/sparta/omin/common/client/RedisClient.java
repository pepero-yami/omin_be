package com.sparta.omin.common.client;

import java.util.Optional;

public interface RedisClient<K, V> {

	Optional<V> get(K key);

	void put(K key, V value);

	boolean delete(K key);
}
