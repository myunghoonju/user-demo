package org.edu.user_demo.application.service;

import lombok.RequiredArgsConstructor;
import org.edu.user_demo.application.port.in.AdminMemberUseCase;
import org.edu.user_demo.application.port.in.MemberSearchCondition;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.exception.MemberNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminMemberService implements AdminMemberUseCase {

    private final LoadMemberPort loadMemberPort;

    @Override
    public Page<Member> getMembers(MemberSearchCondition condition, Pageable pageable) {
        return loadMemberPort.search(condition, pageable);
    }

    @Override
    public Member getMember(Long memberId) {
        return loadMemberPort.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(MemberConstants.MEMBER_NOT_FOUND));
    }
}
