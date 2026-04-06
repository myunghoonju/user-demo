package org.edu.user_demo.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import org.edu.user_demo.application.port.out.PublishSignupPort;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.edu.user_demo.adapter.out.messaging.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SignupMessagePublisher implements PublishSignupPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public String publish(Email email, Password password, String name, PhoneNumber phoneNumber) {
        String requestId = UUID.randomUUID().toString();

        SignupMessage message = new SignupMessage(
                requestId,
                email.getValue(),
                password.getValue(),
                name,
                phoneNumber.getValue()
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.SIGNUP_EXCHANGE, RabbitMQConfig.SIGNUP_QUEUE, message);

        return requestId;
    }
}
