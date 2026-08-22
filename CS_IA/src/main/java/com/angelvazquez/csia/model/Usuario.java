package com.angelvazquez.csia.model;

/** Representa una fila de la tabla USERS del modelo de datos v2. */
public class Usuario {

    private Integer id;
    private String username;
    private String passwordHash;

    public Usuario(Integer id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Usuario(String username, String passwordHash) {
        this(null, username, passwordHash);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
