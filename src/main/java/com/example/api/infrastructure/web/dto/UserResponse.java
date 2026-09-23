package com.example.api.infrastructure.web.dto;

import com.example.api.domain.model.User;

/**
 * Datos que salen al cliente.
 * Nunca incluye la contrasena, aunque este cifrada.
 */
public record UserResponse(Long id, String name, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
