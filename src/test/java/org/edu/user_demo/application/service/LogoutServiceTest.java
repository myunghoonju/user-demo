package org.edu.user_demo.application.service;

import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.domain.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private TokenStorePort tokenStorePort;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    void 로그아웃_시_액세스토큰_블랙리스트_등록() {
        given(jwtTokenProvider.isValid("access-token")).willReturn(true);

        logoutService.logout(1L, "access-token");

        verify(tokenStorePort).blacklistAccessToken(eq("access-token"), any(Duration.class));
    }

    @Test
    void 로그아웃_시_리프레시토큰_삭제() {
        given(jwtTokenProvider.isValid("access-token")).willReturn(true);

        logoutService.logout(1L, "access-token");

        verify(tokenStorePort).deleteRefreshToken(1L);
    }

    @Test
    void 유효하지_않은_토큰으로_로그아웃_시_예외() {
        given(jwtTokenProvider.isValid("invalid-token")).willReturn(false);

        assertThatThrownBy(
                () -> logoutService.logout(1L, "invalid-token"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
