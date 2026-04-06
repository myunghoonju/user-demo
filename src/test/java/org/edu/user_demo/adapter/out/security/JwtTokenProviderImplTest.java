package org.edu.user_demo.adapter.out.security;

import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderImplTest {

    private JwtTokenProviderImpl jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProviderImpl(
                "test-secret-key-that-is-long-enough-for-hs256-algorithm",
                15,
                10080
        );
    }

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
    void 액세스_토큰_생성_성공() {
        String token = jwtTokenProvider.generateAccessToken(createMember());
        assertThat(token).isNotBlank();
    }

    @Test
    void 리프레시_토큰_생성_성공() {
        String token = jwtTokenProvider.generateRefreshToken(createMember());
        assertThat(token).isNotBlank();
    }

    @Test
    void 유효한_토큰_검증_성공() {
        String token = jwtTokenProvider.generateAccessToken(createMember());
        assertThat(jwtTokenProvider.isValid(token)).isTrue();
    }

    @Test
    void 잘못된_토큰_검증_실패() {
        assertThat(jwtTokenProvider.isValid("invalid.token.value")).isFalse();
    }

    @Test
    void 토큰에서_memberId_추출_성공() {
        String token = jwtTokenProvider.generateAccessToken(createMember());
        Long memberId = jwtTokenProvider.extractMemberId(token);
        assertThat(memberId).isEqualTo(1L);
    }

    @Test
    void 비밀번호_인코딩_후_검증_성공() {
        String encoded = jwtTokenProvider.encodePassword("Password1!");
        assertThat(jwtTokenProvider.verifyPassword("Password1!", Password.ofEncoded(encoded))).isTrue();
    }

    @Test
    void 잘못된_비밀번호_검증_실패() {
        String encoded = jwtTokenProvider.encodePassword("Password1!");
        assertThat(jwtTokenProvider.verifyPassword("WrongPass1!", Password.ofEncoded(encoded))).isFalse();
    }
}
