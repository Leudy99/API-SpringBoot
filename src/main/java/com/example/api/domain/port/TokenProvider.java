package com.example.api.domain.port;

/**
 * Puerto de salida para generar tokens.
 * El dominio dice "necesito un token para este email", pero no sabe que es JWT.
 * La implementacion concreta (JwtService) vive en infrastructure/security.
 */
public interface TokenProvider {

    String generateToken(String email);
}
