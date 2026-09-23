package com.example.api.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos que entran al actualizar un usuario.
 * La contrasena es opcional: si no se envia, se mantiene la que ya tenia.
 */
public record UpdateUserRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 150, message = "El email no puede pasar de 150 caracteres")
        String email,

        @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
        String password
) {
}
