package org.edu.user_demo.adapter.in.web;

import org.edu.user_demo.adapter.in.web.config.SecurityConfig;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.edu.user_demo.domain.exception.InvalidCredentialsException;
import org.edu.user_demo.domain.exception.MemberNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.edu.user_demo.application.port.in.ManageMemberUseCase;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@WebMvcTest(MemberController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageMemberUseCase manageMemberUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenStorePort tokenStorePort;

    private UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void 회원_없음_예외_404_반환() throws Exception {
        given(manageMemberUseCase.getMyInfo(1L)).willThrow(new MemberNotFoundException("회원을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/v1/members/me").with(authentication(userAuth())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
    }

    @Test
    void 인증_실패_예외_401_반환() throws Exception {
        given(manageMemberUseCase.getMyInfo(1L)).willThrow(new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        mockMvc.perform(get("/api/v1/members/me").with(authentication(userAuth())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void IllegalArgumentException_400_반환() throws Exception {
        given(manageMemberUseCase.getMyInfo(1L)).willThrow(new IllegalArgumentException("잘못된 요청입니다."));

        mockMvc.perform(get("/api/v1/members/me").with(authentication(userAuth())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
    }

    @Test
    void IllegalStateException_409_반환() throws Exception {
        given(manageMemberUseCase.getMyInfo(1L)).willThrow(new IllegalStateException("이미 탈퇴한 회원입니다."));

        mockMvc.perform(get("/api/v1/members/me").with(authentication(userAuth())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 탈퇴한 회원입니다."));
    }
}
