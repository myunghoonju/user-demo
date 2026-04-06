package org.edu.user_demo.adapter.out.persistence;

import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.WithdrawnMember;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberPersistenceAdapterTest {

    @Mock
    private MemberJpaRepository memberJpaRepository;

    @Mock
    private WithdrawnMemberJpaRepository withdrawnMemberJpaRepository;

    @InjectMocks
    private MemberPersistenceAdapter memberPersistenceAdapter;

    private Member createMember() {
        return Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );
    }

    @Test
    void 회원_저장_성공() {
        Member member = createMember();
        MemberJpaEntity entity = MemberJpaEntity.from(member);
        given(memberJpaRepository.save(any())).willReturn(entity);

        Member saved = memberPersistenceAdapter.save(member);

        assertThat(saved.getEmail().getValue()).isEqualTo("test@example.com");
    }

    @Test
    void 이메일로_회원_조회_성공() {
        Member member = createMember();
        MemberJpaEntity entity = MemberJpaEntity.from(member);
        given(memberJpaRepository.findByEmail("test@example.com")).willReturn(Optional.of(entity));

        Optional<Member> result = memberPersistenceAdapter.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail().getValue()).isEqualTo("test@example.com");
    }

    @Test
    void 존재하지_않는_이메일_조회_시_빈값_반환() {
        given(memberJpaRepository.findByEmail("none@example.com")).willReturn(Optional.empty());

        Optional<Member> result = memberPersistenceAdapter.findByEmail("none@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void 탈퇴_회원_저장_성공() {
        WithdrawnMember withdrawnMember = WithdrawnMember.of(
                1L,
                Email.of("test@example.com"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );
        given(withdrawnMemberJpaRepository.save(any())).willReturn(WithdrawnMemberJpaEntity.from(withdrawnMember));

        memberPersistenceAdapter.saveWithdrawn(withdrawnMember);

        verify(withdrawnMemberJpaRepository).save(any());
    }
}
