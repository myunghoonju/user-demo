package org.edu.user_demo.application.service;

import org.edu.user_demo.application.port.in.MemberSearchCondition;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.exception.MemberNotFoundException;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

    @Mock
    private LoadMemberPort loadMemberPort;

    @InjectMocks
    private AdminMemberService adminMemberService;

    private Member createMember() {
        return Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );
    }

    @Test
    void 회원_목록_조회_성공() {
        PageRequest pageable = PageRequest.of(0, 10);
        MemberSearchCondition condition = MemberSearchCondition.empty();
        Page<Member> page = new PageImpl<>(List.of(createMember()));
        given(loadMemberPort.search(condition, pageable)).willReturn(page);

        Page<Member> result = adminMemberService.getMembers(condition, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void 이름_필터로_회원_목록_조회_성공() {
        PageRequest pageable = PageRequest.of(0, 10);
        MemberSearchCondition condition = MemberSearchCondition.of("홍", null, null);
        Page<Member> page = new PageImpl<>(List.of(createMember()));
        given(loadMemberPort.search(condition, pageable)).willReturn(page);

        Page<Member> result = adminMemberService.getMembers(condition, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void 특정_회원_상세_조회_성공() {
        Member member = createMember();
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));

        Member result = adminMemberService.getMember(1L);

        assertThat(result.getEmail().getValue()).isEqualTo("test@example.com");
    }

    @Test
    void 존재하지_않는_회원_상세_조회_시_예외() {
        given(loadMemberPort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminMemberService.getMember(99L))
                .isInstanceOf(MemberNotFoundException.class);
    }
}
