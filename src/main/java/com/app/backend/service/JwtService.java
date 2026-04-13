package com.app.backend.service;

import com.app.backend.common.BizException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final String issuer;
    private final long expireSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer:backend}") String issuer,
            @Value("${app.jwt.expire-seconds:7200}") long expireSeconds
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expireSeconds = expireSeconds;
        this.verifier = JWT.require(this.algorithm).withIssuer(this.issuer).build();
    }

    public String createToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expireSeconds);
        return JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .withClaim("uid", userId)
                .withClaim("un", username)
                .sign(algorithm);
    }

    public String createToken(Long userId, String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expireSeconds);
        return JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .withClaim("uid", userId)
                .withClaim("un", username)
                .withClaim("role", role)
                .sign(algorithm);
    }

    public DecodedJWT verify(String token) {
        try {
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new BizException(401, "未登录");
        }
    }
}
