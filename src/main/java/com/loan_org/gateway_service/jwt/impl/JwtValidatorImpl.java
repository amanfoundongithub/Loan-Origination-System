package com.loan_org.gateway_service.jwt.impl;

import com.loan_org.gateway_service.jwt.JwtValidator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class JwtValidatorImpl implements JwtValidator {

    @Value("${jwt.signing_key}")
    private String signingKey;

    private SecretKey secretKey;

    @PostConstruct
    private void init() {
        secretKey = Keys.hmacShaKeyFor(
                signingKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public void validate(String jwtToken) {
        Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

}
