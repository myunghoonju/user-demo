package org.edu.user_demo.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.edu.user_demo.adapter.in.web.config.SecurityConfig;
import org.edu.user_demo.application.port.in.AdminMemberUseCase;
import org.edu.user_demo.application.port.out.TokenStorePort;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(AdminMemberController.class)
class AdminMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminMemberUseCase adminMemberUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenStorePort tokenStorePort;

    private Member createMember(Long id, String email, String name) {
        Member member = Member.create(
                Email.of(email),
                Password.of("Password1!"),
                name,
                PhoneNumber.of("01012345678")
        );
        member.assignId(id);
        return member;
    }

    private UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                2L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void 회원_목록_조회_성공() throws Exception {
        List<Member> members = List.of(
                createMember(1L, "admin@example.com", "관리자"),
                createMember(2L, "user@example.com", "사용자")
        );
        given(adminMemberUseCase.getMembers(any(), any())).willReturn(
                new PageImpl<>(members, PageRequest.of(0, 10), 2));

        mockMvc.perform(get("/api/v1/admin/members").with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$.content[1].email").value("user@example.com"));
    }

    @Test
    void 이름_필터로_회원_목록_조회_성공() throws Exception {
        List<Member> members = List.of(createMember(1L, "test@example.com", "홍길동"));
        given(adminMemberUseCase.getMembers(any(), any())).willReturn(
                new PageImpl<>(members, PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/admin/members").param("name", "홍").with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("홍길동"));
    }

    @Test
    void 회원_단건_조회_성공() throws Exception {
        given(adminMemberUseCase.getMember(1L)).willReturn(
                createMember(1L, "test@example.com", "홍길동"));

        mockMvc.perform(get("/api/v1/admin/members/1").with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    void ADMIN_권한_없이_회원_목록_조회_403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/members").with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void 인증_없이_회원_목록_조회_401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/members"))
                .andExpect(status().isUnauthorized());
    }
}
