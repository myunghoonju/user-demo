package org.edu.user_demo.adapter.out.cache;

import lombok.RequiredArgsConstructor;
import org.edu.user_demo.application.port.out.LockPort;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockAdapter implements LockPort {

    private final LockRegistry lockRegistry;

    @Override
    public boolean tryLock(String key) {
        return lockRegistry.obtain(key).tryLock();
    }

    @Override
    public void unlock(String key) {
        lockRegistry.obtain(key).unlock();
    }
}
