package org.edu.user_demo.application.port.in;

public interface RefreshTokenUseCase {

    RefreshTokenResult refresh(String refreshToken);
}
