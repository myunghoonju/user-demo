package org.edu.user_demo.application.service;

import java.time.Duration;

public final class TokenConstants {

    public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private TokenConstants() {
    }
}
