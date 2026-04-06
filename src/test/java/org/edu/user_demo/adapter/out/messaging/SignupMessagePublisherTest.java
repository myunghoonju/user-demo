package org.edu.user_demo.adapter.out.messaging;

import org.edu.user_demo.adapter.out.messaging.config.RabbitMQConfig;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignupMessagePublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SignupMessagePublisher signupMessagePublisher;

    @Test
    void 회원가입_메시지_발행_성공() {
        ArgumentCaptor<SignupMessage> captor = ArgumentCaptor.forClass(SignupMessage.class);

        String requestId = signupMessagePublisher.publish(
                Email.of("test@example.com"),
                Password.of("Password1!"),
                "홍길동",
                PhoneNumber.of("01012345678")
        );

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.SIGNUP_EXCHANGE),
                eq(RabbitMQConfig.SIGNUP_QUEUE),
                captor.capture()
        );

        assertThat(requestId).isNotBlank();
        assertThat(captor.getValue().getEmail()).isEqualTo("test@example.com");
        assertThat(captor.getValue().getName()).isEqualTo("홍길동");
    }
}
