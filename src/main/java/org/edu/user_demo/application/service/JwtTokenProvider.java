package org.edu.user_demo.application.service;

import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.vo.Password;

public interface JwtTokenProvider {

    String generateAccessToken(Member member);

    String generateRefreshToken(Member member);

    boolean verifyPassword(String rawPassword, Password encodedPassword);

    Long extractMemberId(String token);

    boolean isValid(String token);

    String encodePassword(String rawPassword);

    String extractRole(String token);
}
