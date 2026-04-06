package org.edu.user_demo.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.edu.user_demo.adapter.in.web.config.SecurityConfig;
import org.edu.user_demo.application.port.in.ManageMemberUseCase;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageMemberUseCase manageMemberUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenStorePort tokenStorePort;

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

    private UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void 내_정보_조회_성공() throws Exception {
        given(manageMemberUseCase.getMyInfo(1L)).willReturn(createMember());

        mockMvc.perform(get("/api/v1/members/me").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    void 인증_없이_내_정보_조회_401() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 내_정보_수정_성공() throws Exception {
        mockMvc.perform(put("/api/v1/members/me")
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "김철수",
                                "phoneNumber", "01098765432"
                        ))))
                .andExpect(status().isNoContent());

        verify(manageMemberUseCase).updateMyInfo(eq(1L), any());
    }

    @Test
    void 회원_탈퇴_성공() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me")
                        .with(authentication(userAuth()))
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isNoContent());

        verify(manageMemberUseCase).withdraw(eq(1L), any());
    }
}
