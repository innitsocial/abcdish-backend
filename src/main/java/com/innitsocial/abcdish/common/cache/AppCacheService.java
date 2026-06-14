package com.innitsocial.abcdish.common.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.exceptions.JedisException;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class AppCacheService {

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String keyPrefix;
    private JedisPooled jedis;

    public AppCacheService(
            ObjectMapper objectMapper,
            @Value("${app.cache.redis.enabled:false}") boolean enabled,
            @Value("${app.cache.redis.url:}") String redisUrl,
            @Value("${app.cache.redis.key-prefix:abcdish}") String keyPrefix
    ) {
        this.objectMapper = objectMapper;
        this.enabled = enabled && redisUrl != null && !redisUrl.isBlank();
        this.keyPrefix = cleanKeyPart(keyPrefix);

        if (!this.enabled) {
            log.info("Redis cache disabled. Backend will use database reads only.");
            return;
        }

        try {
            this.jedis = new JedisPooled(URI.create(redisUrl));
            this.jedis.ping();
            log.info("Redis cache enabled prefix={}", this.keyPrefix);
        } catch (RuntimeException error) {
            this.jedis = null;
            log.warn("Redis cache could not be initialised. Continuing without cache: {}", error.getMessage());
        }
    }

    public <T> Optional<T> get(String key, TypeReference<T> type) {
        if (!isAvailable()) {
            return Optional.empty();
        }

        try {
            String value = jedis.get(fullKey(key));
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(value, type));
        } catch (Exception error) {
            log.warn("Redis cache read failed key={}: {}", key, error.getMessage());
            return Optional.empty();
        }
    }

    public void set(String key, Object value, Duration ttl) {
        if (!isAvailable() || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }

        try {
            jedis.setex(fullKey(key), Math.toIntExact(Math.min(ttl.toSeconds(), Integer.MAX_VALUE)),
                    objectMapper.writeValueAsString(value));
        } catch (Exception error) {
            log.warn("Redis cache write failed key={}: {}", key, error.getMessage());
        }
    }

    public void evictPrefix(String prefix) {
        if (!isAvailable()) {
            return;
        }

        String pattern = fullKey(prefix) + "*";
        try {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams params = new ScanParams().match(pattern).count(500);
            do {
                ScanResult<String> result = jedis.scan(cursor, params);
                if (!result.getResult().isEmpty()) {
                    jedis.del(result.getResult().toArray(String[]::new));
                }
                cursor = result.getCursor();
            } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
        } catch (JedisException error) {
            log.warn("Redis cache prefix eviction failed prefix={}: {}", prefix, error.getMessage());
        }
    }

    public boolean isAvailable() {
        return enabled && jedis != null;
    }

    @PreDestroy
    void close() {
        if (jedis != null) {
            jedis.close();
        }
    }

    private String fullKey(String key) {
        return keyPrefix + ":" + cleanKeyPart(key);
    }

    private String cleanKeyPart(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", "-");
    }
}
