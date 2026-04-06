package org.edu.user_demo.adapter.out.messaging;

import org.edu.user_demo.application.port.out.LockPort;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.SaveMemberPort;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.edu.user_demo.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * 대량 동시 회원가입 요청에 대한 데이터 정합성 검증 테스트.
 *
 * 실제 Redis 없이 ReentrantLock 기반 인메모리 LockPort 구현체를 사용하여
 * 분산락의 상호 배제 동작을 재현합니다.
 * 동일 이메일로 N개의 동시 요청이 들어올 때 단 1건만 저장됨을 검증합니다.
 */
class SignupConcurrencyTest {

    private SignupConsumer signupConsumer;
    private AtomicInteger saveCount;
    private AtomicInteger duplicateRejectedCount;
    private AtomicInteger lockFailedCount;

    /**
     * 인메모리 분산락 구현체.
     * ReentrantLock을 키별로 관리하며 tryLock()의 비블로킹 동작을 재현합니다.
     */
    static class InMemoryLockPort implements LockPort {
        private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

        @Override
        public boolean tryLock(String key) {
            ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
            return lock.tryLock();
        }

        @Override
        public void unlock(String key) {
            ReentrantLock lock = locks.get(key);
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @BeforeEach
    void setUp() {
        saveCount = new AtomicInteger(0);
        duplicateRejectedCount = new AtomicInteger(0);
        lockFailedCount = new AtomicInteger(0);

        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);

        given(loadMemberPort.existsByEmail(anyString())).willAnswer(inv -> saveCount.get() > 0);
        given(loadMemberPort.existsByPhoneNumber(anyString())).willReturn(false);
        given(jwtTokenProvider.encodePassword(anyString())).willReturn("encoded");
        given(saveMemberPort.save(org.mockito.ArgumentMatchers.any(Member.class))).willAnswer(inv -> {
            saveCount.incrementAndGet();
            return inv.getArgument(0);
        });

        signupConsumer = new SignupConsumer(saveMemberPort, loadMemberPort, new InMemoryLockPort(), jwtTokenProvider);
    }

    @Test
    void 동일_이메일_100개_동시_요청_중_1건만_저장() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    SignupMessage message = new SignupMessage(
                            "req-" + Thread.currentThread().getId(),
                            "duplicate@example.com",
                            "Password1!",
                            "홍길동",
                            "01012345678"
                    );
                    try {
                        signupConsumer.consume(message);
                    } catch (IllegalStateException e) {
                        lockFailedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        long startTime = System.currentTimeMillis();
        start.countDown();
        done.await();
        long elapsed = System.currentTimeMillis() - startTime;

        executor.shutdown();

        System.out.printf("[동일 이메일 동시성 테스트]%n");
        System.out.printf("  총 요청: %d건%n", threadCount);
        System.out.printf("  저장 성공: %d건%n", saveCount.get());
        System.out.printf("  락 획득 실패 (DLQ 이동): %d건%n", lockFailedCount.get());
        System.out.printf("  중복 거부: %d건%n", threadCount - saveCount.get() - lockFailedCount.get());
        System.out.printf("  소요 시간: %dms%n", elapsed);

        assertThat(saveCount.get()).isEqualTo(1);
    }

    @Test
    void 서로_다른_이메일_100개_동시_요청_모두_저장() throws InterruptedException {
        int threadCount = 100;

        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        AtomicInteger uniqueSaveCount = new AtomicInteger(0);

        given(loadMemberPort.existsByEmail(anyString())).willReturn(false);
        given(loadMemberPort.existsByPhoneNumber(anyString())).willReturn(false);
        given(jwtTokenProvider.encodePassword(anyString())).willReturn("encoded");
        given(saveMemberPort.save(org.mockito.ArgumentMatchers.any(Member.class))).willAnswer(inv -> {
            uniqueSaveCount.incrementAndGet();
            return inv.getArgument(0);
        });

        SignupConsumer consumer = new SignupConsumer(
                saveMemberPort, loadMemberPort, new InMemoryLockPort(), jwtTokenProvider);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    SignupMessage message = new SignupMessage(
                            "req-" + idx,
                            "user" + idx + "@example.com",
                            "Password1!",
                            "회원" + idx,
                            "0101234" + String.format("%04d", idx)
                    );
                    consumer.consume(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        long startTime = System.currentTimeMillis();
        start.countDown();
        done.await();
        long elapsed = System.currentTimeMillis() - startTime;

        executor.shutdown();

        System.out.printf("[고유 이메일 동시성 테스트]%n");
        System.out.printf("  총 요청: %d건%n", threadCount);
        System.out.printf("  저장 성공: %d건%n", uniqueSaveCount.get());
        System.out.printf("  소요 시간: %dms%n", elapsed);
        System.out.printf("  처리량(TPS 근사): %.0f req/s%n",
                uniqueSaveCount.get() / Math.max(elapsed / 1000.0, 0.001));

        assertThat(uniqueSaveCount.get()).isEqualTo(threadCount);
    }
}
