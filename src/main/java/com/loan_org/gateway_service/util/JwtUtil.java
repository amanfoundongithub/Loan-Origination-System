package com.loan_org.gateway_service.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String jwtKey;

    @Value("${jwt.metadata.expiration_in_minutes}")
    private long jwtExpirationInMinutes;

    @Value("${jwt.metadata.issuer}")
    private String issuer;

    private static final long CONVERSION_FACTOR_TO_MILLISECONDS = 60000;

    private SecretKey signingKey;

    @PostConstruct
    public void setSecretKey() {
        signingKey = Keys.hmacShaKeyFor(
                jwtKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(String username) {
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + jwtExpirationInMinutes * CONVERSION_FACTOR_TO_MILLISECONDS);

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith(signingKey)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            return getClaims(token).getExpiration().after(new Date());
        } catch (Exception exception) {
            return false;
        }
    }
}
