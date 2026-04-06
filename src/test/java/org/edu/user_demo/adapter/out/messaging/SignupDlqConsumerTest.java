package org.edu.user_demo.adapter.out.messaging;

import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.SaveMemberPort;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SignupDlqConsumerTest {

    @Mock
    private LoadMemberPort loadMemberPort;

    @Mock
    private SaveMemberPort saveMemberPort;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private SignupDlqConsumer dlqConsumer;

    @Test
    void DLQ_메시지_수신_시_예외_없이_처리() {
        SignupMessage message = new SignupMessage(
                "request-id-fail",
                "fail@example.com",
                "Password1!",
                "홍길동",
                "01099998888"
        );

        given(loadMemberPort.existsByEmail(message.getEmail())).willReturn(false);
        given(loadMemberPort.existsByPhoneNumber(message.getPhoneNumber())).willReturn(false);
        given(jwtTokenProvider.encodePassword(message.getPassword())).willReturn("$2a$encoded");

        assertThatCode(() -> dlqConsumer.consume(message))
                .doesNotThrowAnyException();
    }
}
