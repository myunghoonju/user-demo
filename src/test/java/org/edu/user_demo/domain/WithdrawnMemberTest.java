package org.edu.user_demo.domain;

import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WithdrawnMemberTest {

    @Test
    void 탈퇴회원_생성_성공() {
        WithdrawnMember withdrawn = WithdrawnMember.of(
                1L,
                Email.of("test@example.com"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );

        assertThat(withdrawn.getOriginalMemberId()).isEqualTo(1L);
        assertThat(withdrawn.getEmail().getValue()).isEqualTo("test@example.com");
        assertThat(withdrawn.getWithdrawnAt()).isNotNull();
        assertThat(withdrawn.getScheduledDeletionAt()).isAfter(withdrawn.getWithdrawnAt());
    }

    @Test
    void originalMemberId가_null이면_예외() {
        assertThatThrownBy(() -> WithdrawnMember.of(
                null,
                Email.of("test@example.com"),
                "홍길동",
                PhoneNumber.of("01012345678")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 삭제_예정일은_탈퇴일로부터_1년후() {
        WithdrawnMember withdrawn = WithdrawnMember.of(
                1L,
                Email.of("test@example.com"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );

        assertThat(withdrawn.getScheduledDeletionAt())
                .isEqualTo(withdrawn.getWithdrawnAt().plusYears(1));
    }
}
