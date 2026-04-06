package org.edu.user_demo.adapter.out.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupMessage {

    private String requestId;
    private String email;
    private String password;
    private String name;
    private String phoneNumber;
}
