package com.example.api.domain.model;

/**
 * Modelo de dominio. Java puro: no tiene anotaciones de JPA ni de Spring.
 * Representa el concepto "Usuario" del negocio, sin saber en que base de datos se guarda.
 */
public class User {

    private Long id;
    private String name;
    private String email;
    private String password; // siempre cifrada con BCrypt

    public User() {
    }

    public User(Long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
