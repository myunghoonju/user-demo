package org.edu.user_demo.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.edu.user_demo.adapter.in.web.config.SecurityConfig;
import org.edu.user_demo.application.port.in.LoginResult;
import org.edu.user_demo.application.port.in.LoginUseCase;
import org.edu.user_demo.application.port.in.LogoutUseCase;
import org.edu.user_demo.application.port.in.RefreshTokenUseCase;
import org.edu.user_demo.application.port.in.RegisterMemberUseCase;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.application.service.JwtTokenProvider;
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
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterMemberUseCase registerMemberUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenStorePort tokenStorePort;

    @Test
    void 회원가입_요청_202_반환() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "test@example.com",
                                "password", "Password1!",
                                "name", "홍길동",
                                "phoneNumber", "01012345678"
                        ))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    void 회원가입_필수값_누락_400_반환() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "test@example.com"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 로그인_성공_200_반환() throws Exception {
        given(loginUseCase.login(any())).willReturn(new LoginResult("access-token", "refresh-token", "test@example.com", "홍길동"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "test@example.com",
                                "password", "Password1!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"));
    }
}
