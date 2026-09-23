package com.example.api.domain.port;

import com.example.api.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida.
 * El dominio declara QUE necesita (guardar, buscar, borrar) pero no COMO se hace.
 * La implementacion vive en infrastructure/persistence.
 */
public interface UserRepository {

    User save(User user);

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteById(Long id);
}
