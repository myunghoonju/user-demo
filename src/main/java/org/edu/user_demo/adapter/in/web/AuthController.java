package org.edu.user_demo.adapter.in.web;

import java.util.Map;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.user_demo.adapter.in.web.dto.LoginRequest;
import org.edu.user_demo.adapter.in.web.dto.LoginResponse;
import org.edu.user_demo.adapter.in.web.dto.RefreshTokenRequest;
import org.edu.user_demo.adapter.in.web.dto.SignupRequest;
import org.edu.user_demo.adapter.in.web.dto.SignupResponse;
import org.edu.user_demo.application.port.in.*;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RegisterMemberUseCase registerMemberUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        registerMemberUseCase.register(new RegisterMemberCommand(
                Email.of(request.getEmail()),
                Password.of(request.getPassword()),
                request.getName(),
                PhoneNumber.of(request.getPhoneNumber())
        ));
        return ResponseEntity.accepted().body(new SignupResponse(request.getEmail(), request.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.getEmail(), request.getPassword()));
        return ResponseEntity.ok(new LoginResponse(result.getAccessToken(), result.getRefreshToken(), result.getEmail(), result.getName()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResult result = refreshTokenUseCase.refresh(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("accessToken", result.getAccessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long memberId,
                                       @RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.substring(BEARER_PREFIX.length());
        logoutUseCase.logout(memberId, accessToken);
        return ResponseEntity.noContent().build();
    }
}
