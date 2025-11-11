package com.universidad.tutorias.application.dto.auth;

import java.time.Instant;

public record AuthTokensResult(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        Instant refreshTokenExpiry,
        UserInfoResponse userInfo
) {
}
