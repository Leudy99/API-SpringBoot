package com.example.api.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de Spring Data JPA.
 * Spring genera la implementacion en tiempo de ejecucion: no escribimos SQL.
 * Los metodos findByEmail / existsByEmail se derivan del nombre del metodo.
 */
public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
