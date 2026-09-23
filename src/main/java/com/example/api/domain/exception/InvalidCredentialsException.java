package com.example.api.domain.exception;

/**
 * Se lanza cuando el email no existe o la contrasena no coincide.
 * El mensaje es generico a proposito: no revelamos cual de los dos fallo.
 * El manejador global la traduce a HTTP 401.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email o contrasena incorrectos");
    }
}
