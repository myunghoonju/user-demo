package org.edu.user_demo.application.service;

import org.edu.user_demo.application.port.in.RegisterMemberCommand;
import org.edu.user_demo.application.port.out.PublishSignupPort;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterMemberServiceTest {

    @Mock
    private PublishSignupPort publishSignupPort;

    @InjectMocks
    private RegisterMemberService registerMemberService;

    @Test
    void 회원가입_요청_큐_발행_성공() {
        RegisterMemberCommand command = new RegisterMemberCommand(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );
        given(publishSignupPort.publish(any(), any(), any(), any())).willReturn("request-id-1234");

        String requestId = registerMemberService.register(command);

        assertThat(requestId).isEqualTo("request-id-1234");
        verify(publishSignupPort).publish(
                command.getEmail(),
                command.getPassword(),
                command.getName(),
                command.getPhoneNumber()
        );
    }
}
