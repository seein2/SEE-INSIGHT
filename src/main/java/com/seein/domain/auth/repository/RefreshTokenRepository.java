package com.seein.domain.auth.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seein.domain.auth.dto.RefreshTokenSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Refresh Token Redis 저장소
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void save(RefreshTokenSession session) {
        Duration ttl = Duration.between(LocalDateTime.now(), session.getExpiresAt());
        if (ttl.isZero() || ttl.isNegative()) {
            ttl = Duration.ofSeconds(1);
        }

        stringRedisTemplate.opsForValue().set(buildKey(session.getMemberId(), session.getTokenId()), writeValue(session), ttl);
    }

    public Optional<RefreshTokenSession> find(Integer memberId, String tokenId) {
        String rawValue = stringRedisTemplate.opsForValue().get(buildKey(memberId, tokenId));
        if (rawValue == null) {
            return Optional.empty();
        }
        return Optional.of(readValue(rawValue));
    }

    public void delete(Integer memberId, String tokenId) {
        stringRedisTemplate.delete(buildKey(memberId, tokenId));
    }

    public void delete(RefreshTokenSession session) {
        delete(session.getMemberId(), session.getTokenId());
    }

    private String buildKey(Integer memberId, String tokenId) {
        return KEY_PREFIX + memberId + ":" + tokenId;
    }

    private String writeValue(RefreshTokenSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Refresh token session serialization failed", e);
        }
    }

    private RefreshTokenSession readValue(String rawValue) {
        try {
            return objectMapper.readValue(rawValue, RefreshTokenSession.class);
        } catch (IOException e) {
            throw new IllegalStateException("Refresh token session deserialization failed", e);
        }
    }
}
