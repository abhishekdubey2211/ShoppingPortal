package com.jodo.portal.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private final Gson gson = new Gson();

	private static final long DEFAULT_EXPIRATION_TIME = 24L; // 24 hours

	public void setWithDefaultExpiration(String key, Object value) {
		setWithExpiration(key, value, DEFAULT_EXPIRATION_TIME, TimeUnit.HOURS);
	}

	// Set key-value pair with custom expiration
	public void setWithExpiration(String key, Object value, long timeout, TimeUnit unit) {
		try {
			String jsonValue = convertObjToString(value);
			redisTemplate.opsForValue().set(key, jsonValue, timeout, unit);
		} catch (Exception e) {
			throw new RuntimeException("Error setting value in Redis", e);
		}
	}

	// Set key-value pair without expiration
	public void set(String key, Object value) {
		try {
			String jsonValue = convertObjToString(value);
			redisTemplate.opsForValue().set(key, jsonValue);
		} catch (Exception e) {
			throw new RuntimeException("Error setting value in Redis", e);
		}
	}

	// Get value by key
	public String get(String key) {
		try {
			return (String) redisTemplate.opsForValue().get(key);
		} catch (Exception e) {
			throw new RuntimeException("Error getting value from Redis", e);
		}
	}

	// Delete key
	public void delete(String key) {
		try {
			redisTemplate.delete(key);
		} catch (Exception e) {
			throw new RuntimeException("Error deleting key from Redis", e);
		}
	}

	// Set hash field value with default expiration of 24 hours
	public void setHashFieldWithDefaultExpiration(String key, String field, Object value) {
		setHashFieldWithExpiration(key, field, value, DEFAULT_EXPIRATION_TIME, TimeUnit.HOURS);
	}

	// Set hash field value with custom expiration
	public void setHashFieldWithExpiration(String key, String field, Object value, long timeout, TimeUnit unit) {
		try {
			String jsonValue = convertObjToString(value);
			redisTemplate.opsForHash().put(key, field, jsonValue);
			redisTemplate.expire(key, timeout, unit); // Set expiration for the hash key
		} catch (Exception e) {
			throw new RuntimeException("Error setting hash field in Redis", e);
		}
	}

	// Set hash field value without expiration
	public void setHashField(String key, String field, Object value) {
		try {
			String jsonValue = convertObjToString(value);
			redisTemplate.opsForHash().put(key, field, jsonValue);
		} catch (Exception e) {
			throw new RuntimeException("Error setting hash field in Redis", e);
		}
	}

	// Get hash field value
	public String getHashField(String key, String field) {
		try {
			return (String) redisTemplate.opsForHash().get(key, field);
		} catch (Exception e) {
			throw new RuntimeException("Error getting hash field from Redis", e);
		}
	}

	// Get all fields and values in a hash
	public Map<Object, Object> getHashEntries(String key) {
		try {
			return redisTemplate.opsForHash().entries(key);
		} catch (Exception e) {
			throw new RuntimeException("Error getting all hash fields from Redis", e);
		}
	}

	public String convertObjToString(Object obj) {
		return new Gson().toJson(obj, new TypeToken<Object>() {
		}.getType());
	}

	public <T> T convertStringToObj(String jsonString, Class<T> clazz) {
		Type type = TypeToken.get(clazz).getType();
		return gson.fromJson(jsonString, type);
	}

	public <T> List<Object> convertStringToObjType(String jsonString, TypeToken<T> typeToken) {
		return gson.fromJson(jsonString, typeToken.getType());
	}
}
