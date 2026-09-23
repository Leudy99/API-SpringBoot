package com.example.api.domain.port;

/**
 * Puerto de salida para cifrado de contrasenas.
 * El dominio dice "necesito cifrar y comparar", pero no sabe que es BCrypt.
 * La implementacion concreta vive en infrastructure/security.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
