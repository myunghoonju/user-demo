package org.edu.user_demo.application.service;

import lombok.RequiredArgsConstructor;
import org.edu.user_demo.application.port.in.ManageMemberUseCase;
import org.edu.user_demo.application.port.in.UpdateMemberCommand;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.SaveMemberPort;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.WithdrawnMember;
import org.edu.user_demo.domain.exception.MemberNotFoundException;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ManageMemberService implements ManageMemberUseCase {


    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final TokenStorePort tokenStorePort;

    @Override
    public Member getMyInfo(Long memberId) {
        return loadMemberPort.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(MemberConstants.MEMBER_NOT_FOUND));
    }

    @Override
    @Transactional
    public void updateMyInfo(Long memberId, UpdateMemberCommand command) {
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(MemberConstants.MEMBER_NOT_FOUND));

        member.update(command.getName(), PhoneNumber.of(command.getPhoneNumber()));
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    public void withdraw(Long memberId, String accessToken) {
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(MemberConstants.MEMBER_NOT_FOUND));

        WithdrawnMember withdrawnMember = WithdrawnMember.of(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhoneNumber()
        );
        saveMemberPort.saveWithdrawn(withdrawnMember);
        saveMemberPort.delete(memberId);
        tokenStorePort.blacklistAccessToken(accessToken, TokenConstants.ACCESS_TOKEN_TTL);
        tokenStorePort.deleteRefreshToken(memberId);
    }
}
