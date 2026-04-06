package org.edu.user_demo.application.service;

import org.edu.user_demo.application.port.in.RegisterMemberCommand;
import org.edu.user_demo.application.port.in.RegisterMemberUseCase;
import org.edu.user_demo.application.port.out.PublishSignupPort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterMemberService implements RegisterMemberUseCase {

    private final PublishSignupPort publishSignupPort;

    @Override
    public String register(RegisterMemberCommand command) {
        return publishSignupPort.publish(
                command.getEmail(),
                command.getPassword(),
                command.getName(),
                command.getPhoneNumber()
        );
    }
}
