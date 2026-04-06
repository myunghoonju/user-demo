package org.edu.user_demo.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.edu.user_demo.adapter.in.web.config.SecurityConfig;
import org.edu.user_demo.application.port.in.RefreshTokenResult;
import org.edu.user_demo.application.port.in.RefreshTokenUseCase;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.edu.user_demo.domain.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(AuthController.class)
class AuthRefreshControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private org.edu.user_demo.application.port.in.RegisterMemberUseCase registerMemberUseCase;

    @MockitoBean
    private org.edu.user_demo.application.port.in.LoginUseCase loginUseCase;

    @MockitoBean
    private org.edu.user_demo.application.port.in.LogoutUseCase logoutUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenStorePort tokenStorePort;

    @Test
    void 토큰_재발급_성공() throws Exception {
        given(refreshTokenUseCase.refresh(any())).willReturn(new RefreshTokenResult("new-access-token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "valid-refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    void 유효하지_않은_토큰_재발급_401() throws Exception {
        given(refreshTokenUseCase.refresh(any())).willThrow(new InvalidCredentialsException("유효하지 않은 리프레시 토큰입니다."));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "bad-token"))))
                .andExpect(status().isUnauthorized());
    }
}
