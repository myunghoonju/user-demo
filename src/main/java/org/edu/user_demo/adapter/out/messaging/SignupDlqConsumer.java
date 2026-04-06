package org.edu.user_demo.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.user_demo.adapter.out.messaging.config.RabbitMQConfig;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.SaveMemberPort;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignupDlqConsumer {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final JwtTokenProvider jwtTokenProvider;

    @RabbitListener(queues = RabbitMQConfig.SIGNUP_DLQ, containerFactory = "dlqListenerContainerFactory")
    public void consume(SignupMessage message) {
        log.warn("DLQ 수신: requestId={}, email={}", message.getRequestId(), message.getEmail());

        if (loadMemberPort.existsByEmail(message.getEmail())) {
            log.info("이미 가입된 이메일 - 폐기: requestId={}", message.getRequestId());
            return;
        }

        if (loadMemberPort.existsByPhoneNumber(message.getPhoneNumber())) {
            log.info("이미 가입된 전화번호 - 폐기: requestId={}", message.getRequestId());
            return;
        }

        try {
            String encodedPassword = jwtTokenProvider.encodePassword(message.getPassword());
            Member member = Member.create(
                    Email.of(message.getEmail()),
                    Password.ofEncoded(encodedPassword),
                    message.getName(),
                    PhoneNumber.of(message.getPhoneNumber())
            );
            saveMemberPort.save(member);
            log.info("DLQ 복구 완료: requestId={}", message.getRequestId());
        } catch (DataIntegrityViolationException e) {
            log.info("동시 중복 저장 - 폐기: requestId={}", message.getRequestId());
        }
    }
}
