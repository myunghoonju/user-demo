package org.edu.user_demo.application.service;

import org.edu.user_demo.application.port.in.LoginCommand;
import org.edu.user_demo.application.port.in.LoginResult;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private LoadMemberPort loadMemberPort;

    @Mock
    private TokenStorePort tokenStorePort;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private LoginService loginService;

    @Test
    void 로그인_성공() {
        Member member = Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );
        given(loadMemberPort.findByEmail("test@example.com")).willReturn(Optional.of(member));
        given(jwtTokenProvider.verifyPassword("Password1!", member.getPassword())).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(any())).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(any())).willReturn("refresh-token");

        LoginResult result = loginService.login(new LoginCommand("test@example.com", "Password1!"));

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        verify(tokenStorePort).saveRefreshToken(any(), any(), any());
    }

    @Test
    void 존재하지_않는_이메일_로그인_실패() {
        given(loadMemberPort.findByEmail("none@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(new LoginCommand("none@example.com", "Password1!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void 비밀번호_불일치_로그인_실패() {
        Member member = Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );
        given(loadMemberPort.findByEmail("test@example.com")).willReturn(Optional.of(member));
        given(jwtTokenProvider.verifyPassword("WrongPass1!", member.getPassword())).willReturn(false);

        assertThatThrownBy(() -> loginService.login(new LoginCommand("test@example.com", "WrongPass1!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
