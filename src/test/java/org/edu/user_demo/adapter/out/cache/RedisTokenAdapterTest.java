package org.edu.user_demo.adapter.out.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisTokenAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisTokenAdapter redisTokenAdapter;

    @Test
    void 리프레시_토큰_저장() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        redisTokenAdapter.saveRefreshToken(1L, "refresh-token", Duration.ofDays(7));

        verify(valueOperations).set("refresh:1", "refresh-token", Duration.ofDays(7));
    }

    @Test
    void 리프레시_토큰_조회_성공() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh:1")).willReturn("refresh-token");

        Optional<String> result = redisTokenAdapter.findRefreshToken(1L);

        assertThat(result).contains("refresh-token");
    }

    @Test
    void 리프레시_토큰_없으면_빈값_반환() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh:1")).willReturn(null);

        Optional<String> result = redisTokenAdapter.findRefreshToken(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void 리프레시_토큰_삭제() {
        redisTokenAdapter.deleteRefreshToken(1L);

        verify(redisTemplate).delete("refresh:1");
    }

    @Test
    void 액세스_토큰_블랙리스트_등록() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        redisTokenAdapter.blacklistAccessToken("access-token", Duration.ofMinutes(15));

        verify(valueOperations).set("blacklist:access-token", "true", Duration.ofMinutes(15));
    }

    @Test
    void 블랙리스트_여부_확인() {
        given(redisTemplate.hasKey("blacklist:access-token")).willReturn(true);

        boolean result = redisTokenAdapter.isBlacklisted("access-token");

        assertThat(result).isTrue();
    }
}
