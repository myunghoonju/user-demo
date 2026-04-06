package org.edu.user_demo.application.port.in;

import lombok.Getter;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;

@Getter
public class RegisterMemberCommand {

    private final Email email;
    private final Password password;
    private final String name;
    private final PhoneNumber phoneNumber;

    public RegisterMemberCommand(Email email, Password password, String name, PhoneNumber phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
}
