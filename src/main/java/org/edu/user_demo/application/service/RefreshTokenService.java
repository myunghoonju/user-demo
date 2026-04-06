package org.edu.user_demo.application.service;

import lombok.RequiredArgsConstructor;
import org.edu.user_demo.application.port.in.RefreshTokenResult;
import org.edu.user_demo.application.port.in.RefreshTokenUseCase;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private static final String INVALID_REFRESH_TOKEN = "유효하지 않은 리프레시 토큰입니다.";

    private final TokenStorePort tokenStorePort;
    private final LoadMemberPort loadMemberPort;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public RefreshTokenResult refresh(String refreshToken) {
        if (!jwtTokenProvider.isValid(refreshToken)) {
            throw new InvalidCredentialsException(INVALID_REFRESH_TOKEN);
        }

        Long memberId = jwtTokenProvider.extractMemberId(refreshToken);

        String stored = tokenStorePort.findRefreshToken(memberId)
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_REFRESH_TOKEN));

        if (!stored.equals(refreshToken)) {
            throw new InvalidCredentialsException(INVALID_REFRESH_TOKEN);
        }

        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_REFRESH_TOKEN));

        String newAccessToken = jwtTokenProvider.generateAccessToken(member);

        return new RefreshTokenResult(newAccessToken);
    }
}
