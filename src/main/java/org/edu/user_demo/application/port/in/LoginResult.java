package org.edu.user_demo.application.port.in;

import lombok.Getter;

@Getter
public class LoginResult {

    private final String accessToken;
    private final String refreshToken;
    private final String email;
    private final String name;

    public LoginResult(String accessToken, String refreshToken, String email, String name) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.name = name;
    }
}
