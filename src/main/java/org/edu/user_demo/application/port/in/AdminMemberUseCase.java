package org.edu.user_demo.application.port.in;

import org.edu.user_demo.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminMemberUseCase {

    Page<Member> getMembers(MemberSearchCondition condition, Pageable pageable);

    Member getMember(Long memberId);
}
