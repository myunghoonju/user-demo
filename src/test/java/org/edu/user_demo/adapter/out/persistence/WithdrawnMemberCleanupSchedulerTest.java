package org.edu.user_demo.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WithdrawnMemberCleanupSchedulerTest {

    @Mock
    private WithdrawnMemberJpaRepository withdrawnMemberJpaRepository;

    @InjectMocks
    private WithdrawnMemberCleanupScheduler scheduler;

    @Test
    void 만료_탈퇴회원_삭제() {
        scheduler.run();

        verify(withdrawnMemberJpaRepository).deleteAllByScheduledDeletionAtBefore(any());
    }
}
