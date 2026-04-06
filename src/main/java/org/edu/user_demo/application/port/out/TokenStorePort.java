package org.edu.user_demo.application.port.out;

import java.time.Duration;
import java.util.Optional;

public interface TokenStorePort {

    void saveRefreshToken(Long memberId, String refreshToken, Duration ttl);

    Optional<String> findRefreshToken(Long memberId);

    void deleteRefreshToken(Long memberId);

    void blacklistAccessToken(String accessToken, Duration ttl);

    boolean isBlacklisted(String accessToken);
}
