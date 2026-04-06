package org.edu.user_demo.adapter.out.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.vo.Password;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenMinutes;
    private final long refreshTokenMinutes;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public JwtTokenProviderImpl(@Value("${jwt.secret}")
                                String secret,
                                @Value("${jwt.access-token-minutes}")
                                long accessTokenMinutes,
                                @Value("${jwt.refresh-token-minutes}")
                                long refreshTokenMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenMinutes = refreshTokenMinutes;
    }

    @Override
    public String generateAccessToken(Member member) {
        return buildToken(member, accessTokenMinutes);
    }

    @Override
    public String generateRefreshToken(Member member) {
        return buildToken(member, refreshTokenMinutes);
    }

    @Override
    public boolean verifyPassword(String rawPassword, Password encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword.getValue());
    }

    @Override
    public Long extractMemberId(String token) {
        return parseClaims(token).get("memberId", Long.class);
    }

    @Override
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private String buildToken(Member member, long minutes) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + minutes * 60 * 1000);

        return Jwts.builder()
                   .claim("memberId", member.getId())
                   .claim("role", member.getRole().name())
                   .issuedAt(now)
                   .expiration(expiry)
                   .signWith(secretKey)
                   .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                   .verifyWith(secretKey)
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }
}
