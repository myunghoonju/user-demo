package org.edu.user_demo.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public class PhoneNumber {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^010\\d{8}$");

    private final String value;

    private PhoneNumber(String value) {
        this.value = value;
    }

    public static PhoneNumber of(String value) {
        requireNonBlank(value, "전화번호는 필수입니다.");
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다. (예: 01012345678)");
        }

        return new PhoneNumber(value);
    }

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhoneNumber that)) {
            return false;
        }

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
