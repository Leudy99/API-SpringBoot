package com.example.api.infrastructure.web.dto;

/**
 * Respuesta de POST /auth/login.
 * "type" indica como se debe mandar el token: cabecera Authorization: Bearer <token>
 */
public record AuthResponse(String token, String type) {

    public static AuthResponse of(String token) {
        return new AuthResponse(token, "Bearer");
    }
}
