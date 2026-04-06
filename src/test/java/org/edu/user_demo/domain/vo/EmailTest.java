package org.edu.user_demo.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void 유효한_이메일_생성_성공() {
        Email email = Email.of("test@example.com");
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    void 이메일_null이면_예외() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이메일_형식이_아니면_예외() {
        assertThatThrownBy(() -> Email.of("invalid-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 동일한_이메일_값은_동등() {
        Email e1 = Email.of("test@example.com");
        Email e2 = Email.of("test@example.com");
        assertThat(e1).isEqualTo(e2);
    }
}
