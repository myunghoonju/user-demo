package org.edu.user_demo.adapter.out.messaging;

import org.edu.user_demo.application.port.out.LockPort;
import org.edu.user_demo.application.port.out.SaveMemberPort;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.edu.user_demo.domain.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignupConsumerTest {

    @Mock
    private SaveMemberPort saveMemberPort;

    @Mock
    private LoadMemberPort loadMemberPort;

    @Mock
    private LockPort lockPort;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private SignupConsumer signupConsumer;

    private SignupMessage createMessage() {
        return new SignupMessage(
                "request-id-1234",
                "test@example.com",
                "Password1!",
                "홍길동",
                "01012345678"
        );
    }

    @Test
    void 회원가입_메시지_정상_처리() {
        SignupMessage message = createMessage();
        given(lockPort.tryLock(anyString())).willReturn(true);
        given(loadMemberPort.existsByEmail("test@example.com")).willReturn(false);
        given(loadMemberPort.existsByPhoneNumber("01012345678")).willReturn(false);
        given(jwtTokenProvider.encodePassword(anyString())).willReturn("encoded-password");

        signupConsumer.consume(message);

        verify(saveMemberPort).save(any(Member.class));
        verify(lockPort, times(2)).unlock(anyString());
    }

    @Test
    void 중복_이메일_가입_시_저장_안함() {
        SignupMessage message = createMessage();
        given(lockPort.tryLock(anyString())).willReturn(true);
        given(loadMemberPort.existsByEmail("test@example.com")).willReturn(true);

        signupConsumer.consume(message);

        verify(saveMemberPort, never()).save(any());
        verify(lockPort, times(2)).unlock(anyString());
    }

    @Test
    void 이메일_락_획득_실패_시_예외_발생() {
        SignupMessage message = createMessage();
        given(lockPort.tryLock(anyString())).willReturn(false);

        assertThatThrownBy(() -> signupConsumer.consume(message))
                .isInstanceOf(IllegalStateException.class);

        verify(saveMemberPort, never()).save(any());
        verify(lockPort, never()).unlock(anyString());
    }

    @Test
    void 전화번호_락_획득_실패_시_이메일_락_해제() {
        SignupMessage message = createMessage();
        given(lockPort.tryLock("lock:email:test@example.com")).willReturn(true);
        given(lockPort.tryLock("lock:phone:01012345678")).willReturn(false);

        assertThatThrownBy(() -> signupConsumer.consume(message))
                .isInstanceOf(IllegalStateException.class);

        verify(lockPort).unlock("lock:email:test@example.com");
        verify(saveMemberPort, never()).save(any());
    }
}
