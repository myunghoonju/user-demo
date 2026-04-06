package org.edu.user_demo.domain;

import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    void 회원_생성_성공() {
        Member member = Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );

        assertThat(member.getEmail().getValue()).isEqualTo("test@example.com");
        assertThat(member.getName()).isEqualTo("홍길동");
        assertThat(member.getRole()).isEqualTo(MemberRole.USER);
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void 이름이_null이면_예외() {
        assertThatThrownBy(() -> Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                null,
                PhoneNumber.of("01012345678")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이름이_공백이면_예외() {
        assertThatThrownBy(() -> Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "   ",
                PhoneNumber.of("01012345678")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 회원_정보_수정_성공() {
        Member member = Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );

        member.update("김철수", PhoneNumber.of("01098765432"));

        assertThat(member.getName()).isEqualTo("김철수");
        assertThat(member.getPhoneNumber().getValue()).isEqualTo("01098765432");
    }

    @Test
    void 수정_시_이름이_null이면_예외() {
        Member member = Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );

        assertThatThrownBy(() -> member.update(null, PhoneNumber.of("01098765432")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 회원_탈퇴_시_상태가_WITHDRAWN으로_변경() {
        Member member = Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );

        member.withdraw();

        assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
    }

    @Test
    void 이미_탈퇴한_회원은_재탈퇴_불가() {
        Member member = Member.create(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );

        member.withdraw();

        assertThatThrownBy(member::withdraw)
                .isInstanceOf(IllegalStateException.class);
    }
}
