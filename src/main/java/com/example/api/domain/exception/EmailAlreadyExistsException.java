package com.example.api.domain.exception;

/**
 * Se lanza al intentar registrar un email que ya esta en uso.
 * El manejador global la traduce a HTTP 409 (Conflict).
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("El email ya esta registrado: " + email);
    }
}
