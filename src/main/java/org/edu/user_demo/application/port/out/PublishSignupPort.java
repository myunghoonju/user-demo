package org.edu.user_demo.application.port.out;

import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;

public interface PublishSignupPort {

    String publish(Email email, Password password, String name, PhoneNumber phoneNumber);
}
