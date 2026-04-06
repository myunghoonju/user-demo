package org.edu.user_demo.application.service;

import org.edu.user_demo.application.port.in.RefreshTokenResult;
import org.edu.user_demo.application.port.in.RefreshTokenUseCase;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.exception.InvalidCredentialsException;
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

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private TokenStorePort tokenStorePort;

    @Mock
    private LoadMemberPort loadMemberPort;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private Member createMember() {
        Member member = Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );
        member.assignId(1L);
        return member;
    }

    @Test
    void 리프레시_토큰으로_액세스_토큰_재발급_성공() {
        Member member = createMember();
        given(jwtTokenProvider.isValid("refresh-token")).willReturn(true);
        given(jwtTokenProvider.extractMemberId("refresh-token")).willReturn(1L);
        given(tokenStorePort.findRefreshToken(1L)).willReturn(Optional.of("refresh-token"));
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));
        given(jwtTokenProvider.generateAccessToken(member)).willReturn("new-access-token");

        RefreshTokenResult result = refreshTokenService.refresh("refresh-token");

        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    void 유효하지_않은_리프레시_토큰_예외() {
        given(jwtTokenProvider.isValid("bad-token")).willReturn(false);

        assertThatThrownBy(() -> refreshTokenService.refresh("bad-token"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void 저장된_토큰과_불일치_예외() {
        given(jwtTokenProvider.isValid("refresh-token")).willReturn(true);
        given(jwtTokenProvider.extractMemberId("refresh-token")).willReturn(1L);
        given(tokenStorePort.findRefreshToken(1L)).willReturn(Optional.of("other-token"));

        assertThatThrownBy(() -> refreshTokenService.refresh("refresh-token"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
