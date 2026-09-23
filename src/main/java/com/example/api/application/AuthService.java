package com.example.api.application;

import com.example.api.domain.exception.InvalidCredentialsException;
import com.example.api.domain.model.User;
import com.example.api.domain.port.PasswordHasher;
import com.example.api.domain.port.TokenProvider;
import com.example.api.domain.port.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Casos de uso de autenticacion: registrar e iniciar sesion.
 * Igual que UserService, solo depende de puertos del dominio.
 */
@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public AuthService(UserService userService,
                       UserRepository userRepository,
                       PasswordHasher passwordHasher,
                       TokenProvider tokenProvider) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    /** Registrar: reutiliza el caso de uso de crear usuario (valida email y cifra). */
    public User register(String name, String email, String rawPassword) {
        return userService.create(name, email, rawPassword);
    }

    /**
     * Iniciar sesion: compara la contrasena con el hash guardado.
     * Si coincide, devuelve un JWT. Si no, lanza 401.
     */
    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return tokenProvider.generateToken(user.getEmail());
    }
}
