package org.edu.user_demo.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface WithdrawnMemberJpaRepository extends JpaRepository<WithdrawnMemberJpaEntity, Long> {

    @Transactional
    void deleteAllByScheduledDeletionAtBefore(LocalDateTime threshold);
}
