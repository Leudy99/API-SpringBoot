package com.example.api.infrastructure.security;

import com.example.api.domain.port.TokenProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Adaptador de tokens: implementa TokenProvider usando JWT (libreria JJWT).
 * Es el unico lugar del proyecto que sabe como se firma y se lee un token.
 *
 * El token guarda el email en el campo "subject" y lleva firma HMAC-SHA256.
 * Si alguien modifica el token, la firma deja de coincidir y se rechaza.
 */
@Service
public class JwtService implements TokenProvider {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    /** Crea un token firmado que caduca dentro de jwt.expiration milisegundos. */
    @Override
    public String generateToken(String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /** Lee el email guardado dentro del token. Verifica la firma al hacerlo. */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** true si el token tiene firma valida y no ha caducado. */
    public boolean isValid(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
