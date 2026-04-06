package org.edu.user_demo.application.service;

import lombok.RequiredArgsConstructor;
import org.edu.user_demo.application.port.in.LogoutUseCase;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.domain.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {


    private final TokenStorePort tokenStorePort;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void logout(Long memberId, String accessToken) {
        if (!jwtTokenProvider.isValid(accessToken)) {
            throw new InvalidCredentialsException("유효하지 않은 토큰입니다.");
        }

        tokenStorePort.blacklistAccessToken(accessToken, TokenConstants.ACCESS_TOKEN_TTL);
        tokenStorePort.deleteRefreshToken(memberId);
    }
}
