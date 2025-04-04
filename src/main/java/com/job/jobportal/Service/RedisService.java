package com.job.jobportal.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T get(String key, Class<T> entityClass) {
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data == null) {
                return null;
            }
            return objectMapper.readValue(objectMapper.writeValueAsString(data), entityClass);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving key: " + key, e);
        }
    }

    public <T> List<T> getList(String key, Class<T> entityClass) {
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data == null) {
                return null;
            }
            return objectMapper.readValue(
                    objectMapper.writeValueAsString(data),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving list key: " + key, e);
        }
    }

    public void set(String key, Object value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Error setting key: " + key, e);
        }
    }

    public void setList(String key, List<?> list, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, list, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Error setting list key: " + key, e);
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting key: " + key, e);
        }
    }

    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            throw new RuntimeException("Error checking key existence: " + key, e);
        }
    }

    public void expire(String key, long ttlSeconds) {
        try {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Error setting expiration for key: " + key, e);
        }
    }
}