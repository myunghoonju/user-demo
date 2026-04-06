package org.edu.user_demo.application.port.out;

import org.edu.user_demo.application.port.in.MemberSearchCondition;
import org.edu.user_demo.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoadMemberPort {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByPhoneNumber(String phoneNumber);

    Optional<Member> findById(Long id);

    Page<Member> search(MemberSearchCondition condition, Pageable pageable);

    List<Member> findAllWithdrawn();

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
