package com.coflxl.api.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "CoflxlApiPlatformSecretKey_1234567890";
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24 hours

    public static String createToken(Long userId, String username) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + EXPIRE_TIME);
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(SECRET))
                    .build()
                    .verify(token);
        } catch (Exception e) {
            return null;
        }
    }
}
