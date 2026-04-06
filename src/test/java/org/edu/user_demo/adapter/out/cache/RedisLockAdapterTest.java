package org.edu.user_demo.adapter.out.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.support.locks.LockRegistry;

import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisLockAdapterTest {

    @Mock
    private LockRegistry lockRegistry;

    @Mock
    private Lock lock;

    @InjectMocks
    private RedisLockAdapter redisLockAdapter;

    @Test
    void 락_획득_성공() {
        given(lockRegistry.obtain("lock:email:test@example.com")).willReturn(lock);
        given(lock.tryLock()).willReturn(true);

        boolean result = redisLockAdapter.tryLock("lock:email:test@example.com");

        assertThat(result).isTrue();
    }

    @Test
    void 락_획득_실패() {
        given(lockRegistry.obtain("lock:email:test@example.com")).willReturn(lock);
        given(lock.tryLock()).willReturn(false);

        boolean result = redisLockAdapter.tryLock("lock:email:test@example.com");

        assertThat(result).isFalse();
    }

    @Test
    void 락_해제() {
        given(lockRegistry.obtain("lock:email:test@example.com")).willReturn(lock);

        redisLockAdapter.unlock("lock:email:test@example.com");

        verify(lock).unlock();
    }
}
