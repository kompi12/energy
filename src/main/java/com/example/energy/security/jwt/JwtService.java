package com.example.energy.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(
            @Value("${app.jwt.secretBase64}") String secretBase64,
            @Value("${app.jwt.expirationMinutes}") long expirationMinutes
    ) {
        // ✅ decode base64 -> bytes -> key
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String username, String rolesCsv) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationMinutes * 60);

        return Jwts.builder()
                .subject(username)
                .claim("roles", rolesCsv)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                // ✅ sign with strong key -> HS512 strength ok
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            System.out.println("JWT invalid: " + ex.getClass().getName() + " | " + ex.getMessage());
            return false;
        }
    }
}
