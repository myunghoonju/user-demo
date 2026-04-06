package org.edu.user_demo.domain.vo;

import java.util.regex.Pattern;

public class Password {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$");

    private final String value;

    private Password(String value) {
        this.value = value;
    }

    public static Password of(String value) {
        requireNonBlank(value, "비밀번호는 필수입니다.");
        if (!PASSWORD_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다.");
        }

        return new Password(value);
    }

    public static Password ofEncoded(String encodedValue) {
        requireNonBlank(encodedValue, "비밀번호는 필수입니다.");
        return new Password(encodedValue);
    }

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public String getValue() {
        return value;
    }
}
