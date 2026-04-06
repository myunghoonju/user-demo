package org.edu.user_demo.application.service;

import lombok.RequiredArgsConstructor;
import org.edu.user_demo.application.port.in.LoginCommand;
import org.edu.user_demo.application.port.in.LoginResult;
import org.edu.user_demo.application.port.in.LoginUseCase;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.MemberStatus;
import org.edu.user_demo.domain.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private static final String INVALID_CREDENTIALS = "이메일 또는 비밀번호가 올바르지 않습니다.";

    private final LoadMemberPort loadMemberPort;
    private final TokenStorePort tokenStorePort;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResult login(LoginCommand command) {
        Member member = loadMemberPort.findByEmail(command.getEmail())
                                      .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS));

        if (!jwtTokenProvider.verifyPassword(command.getPassword(), member.getPassword())) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member);
        String refreshToken = jwtTokenProvider.generateRefreshToken(member);

        tokenStorePort.saveRefreshToken(member.getId(), refreshToken, TokenConstants.REFRESH_TOKEN_TTL);

        return new LoginResult(accessToken, refreshToken, member.getEmail().getValue(), member.getName());
    }
}
