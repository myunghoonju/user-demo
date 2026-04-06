package org.edu.user_demo.application.port.in;

import org.edu.user_demo.domain.Member;

public interface ManageMemberUseCase {

    Member getMyInfo(Long memberId);

    void updateMyInfo(Long memberId, UpdateMemberCommand command);

    void withdraw(Long memberId, String accessToken);
}
