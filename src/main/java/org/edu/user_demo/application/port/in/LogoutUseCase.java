package org.edu.user_demo.application.port.in;

public interface LogoutUseCase {

    void logout(Long memberId, String accessToken);
}
