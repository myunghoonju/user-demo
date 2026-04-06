package org.edu.user_demo.adapter.out.persistence;

import org.edu.user_demo.domain.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

    Optional<MemberJpaEntity> findByEmail(String email);

    Optional<MemberJpaEntity> findByPhoneNumber(String phoneNumber);

    boolean existsByEmailAndStatus(String email, MemberStatus status);

    boolean existsByPhoneNumberAndStatus(String phoneNumber, MemberStatus status);

    List<MemberJpaEntity> findAllByStatus(MemberStatus status);
}
