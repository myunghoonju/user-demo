package org.edu.user_demo.application.port.in;

import lombok.Getter;

@Getter
public class LoginCommand {

    private final String email;
    private final String password;

    public LoginCommand(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
