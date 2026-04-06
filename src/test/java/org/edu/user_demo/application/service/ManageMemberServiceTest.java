package org.edu.user_demo.application.service;

import org.edu.user_demo.application.port.in.UpdateMemberCommand;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.SaveMemberPort;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.exception.MemberNotFoundException;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManageMemberServiceTest {

    @Mock
    private LoadMemberPort loadMemberPort;

    @Mock
    private SaveMemberPort saveMemberPort;

    @Mock
    private TokenStorePort tokenStorePort;

    @InjectMocks
    private ManageMemberService manageMemberService;

    private Member createMember() {
        return Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );
    }

    @Test
    void 내_정보_조회_성공() {
        Member member = createMember();
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));

        Member result = manageMemberService.getMyInfo(1L);

        assertThat(result.getEmail().getValue()).isEqualTo("test@example.com");
    }

    @Test
    void 존재하지_않는_회원_조회_시_예외() {
        given(loadMemberPort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> manageMemberService.getMyInfo(99L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 내_정보_수정_성공() {
        Member member = createMember();
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));

        manageMemberService.updateMyInfo(1L, new UpdateMemberCommand("김철수", "01098765432"));

        verify(saveMemberPort).save(any(Member.class));
    }

    @Test
    void 회원_탈퇴_성공() {
        Member member = createMember();
        member.assignId(1L);
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));

        manageMemberService.withdraw(1L, "access-token");

        verify(saveMemberPort).saveWithdrawn(any());
        verify(saveMemberPort).delete(1L);
        verify(tokenStorePort).blacklistAccessToken(any(), any());
        verify(tokenStorePort).deleteRefreshToken(1L);
    }
}
