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
        Object data = redisTemplate.opsForValue().get(key);
        if (data == null)
            return null;
        return objectMapper.convertValue(data, entityClass);
    }

    public <T> List<T> getList(String key, Class<T> entityClass) {
        Object data = redisTemplate.opsForValue().get(key);
        if (data == null)
            return null;
        return objectMapper.convertValue(data,
                objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));
    }

    public void set(String key, Object value, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}