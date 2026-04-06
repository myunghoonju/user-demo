package org.edu.user_demo.application.port.in;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RefreshTokenResult {

    private final String accessToken;
}
