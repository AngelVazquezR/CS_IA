package com.angelvazquez.csia.security;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.angelvazquez.csia.database.repository.UsuarioRepository;
import com.angelvazquez.csia.model.Usuario;

/** Lógica de registro y autenticación independiente de Swing. */
public final class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public AuthService(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
    }

    public int registrar(String username, char[] password) throws SQLException {
        validateUsername(username);
        validatePassword(password);
        try {
            String hash = passwordHasher.hash(password);
            return usuarioRepository.registrar(new Usuario(username, hash));
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public boolean autenticar(String username, char[] password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.length == 0) {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
            return false;
        }

        try {
            Optional<Usuario> usuario = usuarioRepository.buscarPorUsername(username);
            return usuario.isPresent()
                    && passwordHasher.verify(password, usuario.get().getPasswordHash());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El usuario no puede estar vacío.");
        }
    }

    private void validatePassword(char[] password) {
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
    }
}
