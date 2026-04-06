package org.edu.user_demo.adapter.out.cache;

import lombok.RequiredArgsConstructor;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisTokenAdapter implements TokenStorePort {

    private static final String REFRESH_KEY_PREFIX = "refresh:";
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void saveRefreshToken(Long memberId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(refreshKey(memberId), refreshToken, ttl);
    }

    @Override
    public Optional<String> findRefreshToken(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(refreshKey(memberId)));
    }

    @Override
    public void deleteRefreshToken(Long memberId) {
        redisTemplate.delete(refreshKey(memberId));
    }

    @Override
    public void blacklistAccessToken(String accessToken, Duration ttl) {
        redisTemplate.opsForValue().set(blacklistKey(accessToken), "true", ttl);
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(accessToken)));
    }

    private String refreshKey(Long memberId) {
        return REFRESH_KEY_PREFIX + memberId;
    }

    private String blacklistKey(String accessToken) {
        return BLACKLIST_KEY_PREFIX + accessToken;
    }
}
