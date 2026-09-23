package com.example.api.application;

import com.example.api.domain.exception.EmailAlreadyExistsException;
import com.example.api.domain.exception.UserNotFoundException;
import com.example.api.domain.model.User;
import com.example.api.domain.port.PasswordHasher;
import com.example.api.domain.port.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Casos de uso del CRUD de usuarios.
 * Solo depende de los puertos del dominio (UserRepository, PasswordHasher),
 * nunca de JPA ni de HTTP. Aqui vive la logica de negocio.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /** Crear usuario: valida email unico y cifra la contrasena antes de guardar. */
    public User create(String name, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = new User(null, name, email, passwordHasher.hash(rawPassword));
        return userRepository.save(user);
    }

    /** Listar todos los usuarios. */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /** Buscar por id o fallar con 404. */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Actualizar usuario.
     * La contrasena es opcional: si llega vacia o nula, se conserva la actual.
     */
    public User update(Long id, String name, String email, String rawPassword) {
        User existing = findById(id);

        // Si cambia el email, verificar que no lo tenga otro usuario
        if (!existing.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        existing.setName(name);
        existing.setEmail(email);
        if (rawPassword != null && !rawPassword.isBlank()) {
            existing.setPassword(passwordHasher.hash(rawPassword));
        }

        return userRepository.save(existing);
    }

    /** Eliminar usuario: falla con 404 si no existe. */
    public void delete(Long id) {
        User existing = findById(id);
        userRepository.deleteById(existing.getId());
    }
}
