package org.edu.user_demo.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawnMemberCleanupScheduler {

    private final WithdrawnMemberJpaRepository withdrawnMemberJpaRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void run() {
        deleteExpiredWithdrawnMembers();
    }

    private void deleteExpiredWithdrawnMembers() {
        LocalDateTime now = LocalDateTime.now();
        withdrawnMemberJpaRepository.deleteAllByScheduledDeletionAtBefore(now);
        log.info("보존 기간 만료 탈퇴 회원 삭제 완료: threshold={}", now);
    }
}
