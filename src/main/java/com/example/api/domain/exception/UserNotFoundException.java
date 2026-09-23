package com.example.api.domain.exception;

/**
 * Se lanza cuando se busca un usuario que no existe.
 * El manejador global la traduce a HTTP 404.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Usuario no encontrado con id: " + id);
    }
}
