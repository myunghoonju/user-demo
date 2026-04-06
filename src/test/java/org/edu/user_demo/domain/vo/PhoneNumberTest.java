package org.edu.user_demo.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    @Test
    void 유효한_전화번호_생성_성공() {
        PhoneNumber phone = PhoneNumber.of("01012345678");
        assertThat(phone.getValue()).isEqualTo("01012345678");
    }

    @Test
    void 전화번호_null이면_예외() {
        assertThatThrownBy(() -> PhoneNumber.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 전화번호_형식이_아니면_예외() {
        assertThatThrownBy(() -> PhoneNumber.of("0101234"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 전화번호_숫자_외_문자_포함이면_예외() {
        assertThatThrownBy(() -> PhoneNumber.of("010-1234-5678"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 동일한_전화번호_값은_동등() {
        PhoneNumber p1 = PhoneNumber.of("01012345678");
        PhoneNumber p2 = PhoneNumber.of("01012345678");
        assertThat(p1).isEqualTo(p2);
    }
}
