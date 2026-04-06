package org.edu.user_demo.adapter.out.persistence;

import org.edu.user_demo.adapter.out.persistence.config.AesEncryptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AesEncryptorTest {

    private final AesEncryptor encryptor = new AesEncryptor("0123456789abcdef0123456789abcdef");

    @Test
    void 암호화_복호화_일치() {
        String original = "홍길동";
        String encrypted = encryptor.encrypt(original);
        String decrypted = encryptor.decrypt(encrypted);

        assertThat(encrypted).isNotEqualTo(original);
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void 동일_값_암호화_시_다른_결과() {
        String encrypted1 = encryptor.encrypt("홍길동");
        String encrypted2 = encryptor.encrypt("홍길동");

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void 전화번호_암호화_복호화() {
        String phone = "01012345678";
        assertThat(encryptor.decrypt(encryptor.encrypt(phone))).isEqualTo(phone);
    }
}
