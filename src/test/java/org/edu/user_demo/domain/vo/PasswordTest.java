package org.edu.user_demo.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordTest {

    @Test
    void 유효한_비밀번호_생성_성공() {
        Password password = Password.of("Password1!");
        assertThat(password.getValue()).isEqualTo("Password1!");
    }

    @Test
    void 비밀번호_null이면_예외() {
        assertThatThrownBy(() -> Password.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호_8자_미만이면_예외() {
        assertThatThrownBy(() -> Password.of("Pass1!"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호_영문_숫자_특수문자_미포함이면_예외() {
        assertThatThrownBy(() -> Password.of("password123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호는_동등성_비교_불가() {
        Password p1 = Password.of("Password1!");
        Password p2 = Password.of("Password1!");
        assertThat(p1).isNotEqualTo(p2);
    }
}
