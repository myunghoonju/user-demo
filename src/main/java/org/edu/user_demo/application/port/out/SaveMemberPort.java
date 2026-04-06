package org.edu.user_demo.application.port.out;

import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.WithdrawnMember;

public interface SaveMemberPort {

    Member save(Member member);

    WithdrawnMember saveWithdrawn(WithdrawnMember withdrawnMember);

    void delete(Long memberId);
}
