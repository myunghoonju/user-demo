package org.edu.user_demo.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.user_demo.application.port.out.LockPort;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.SaveMemberPort;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.edu.user_demo.adapter.out.messaging.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignupConsumer {

    private static final String EMAIL_LOCK_PREFIX = "lock:email:";
    private static final String PHONE_LOCK_PREFIX = "lock:phone:";

    private final SaveMemberPort saveMemberPort;
    private final LoadMemberPort loadMemberPort;
    private final LockPort lockPort;
    private final JwtTokenProvider jwtTokenProvider;

    @RabbitListener(queues = RabbitMQConfig.SIGNUP_QUEUE)
    public void consume(SignupMessage message) {
        String emailLockKey = EMAIL_LOCK_PREFIX + message.getEmail();
        String phoneLockKey = PHONE_LOCK_PREFIX + message.getPhoneNumber();

        if (!lockPort.tryLock(emailLockKey)) {
            throw new IllegalStateException("락 획득 실패 - 재처리 대상: " + message.getRequestId());
        }

        if (!lockPort.tryLock(phoneLockKey)) {
            lockPort.unlock(emailLockKey);
            throw new IllegalStateException("락 획득 실패 - 재처리 대상: " + message.getRequestId());
        }

        try {
            if (loadMemberPort.existsByEmail(message.getEmail())) {
                log.warn("중복 이메일 가입 시도: {}", message.getEmail());
                return;
            }

            if (loadMemberPort.existsByPhoneNumber(message.getPhoneNumber())) {
                log.warn("중복 전화번호 가입 시도: {}", message.getPhoneNumber());
                return;
            }

            String encodedPassword = jwtTokenProvider.encodePassword(message.getPassword());

            Member member = Member.create(
                    Email.of(message.getEmail()),
                    Password.ofEncoded(encodedPassword),
                    message.getName(),
                    PhoneNumber.of(message.getPhoneNumber())
            );

            saveMemberPort.save(member);
            log.info("회원가입 완료: requestId={}", message.getRequestId());

        } finally {
            lockPort.unlock(emailLockKey);
            lockPort.unlock(phoneLockKey);
        }
    }
}
