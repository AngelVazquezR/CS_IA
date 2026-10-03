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

    /**
     * Valida las credenciales, genera el hash y persiste el nuevo usuario.
     * Tras superar la validación, borra el array de contraseña incluso si falla
     * el hash o la escritura. Si la validación falla, el array no se borra aquí.
     *
     * @param username nombre no nulo ni en blanco; el repositorio lo recorta y convierte a mayúsculas
     * @param password contraseña de al menos ocho caracteres; el array se modifica
     * @return identificador generado para el usuario registrado
     * @throws IllegalArgumentException si las credenciales no son válidas o el usuario ya existe
     * @throws SQLException si falla la persistencia
     * @throws IllegalStateException si no se puede generar el hash
     */
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

    /**
     * Busca el usuario y verifica la contraseña frente al hash almacenado.
     * Borra el array recibido, si no es nulo, también con entradas inválidas
     * o cuando falla la consulta. El llamador no debe reutilizar su contenido.
     *
     * @param username nombre que el repositorio recorta y convierte a mayúsculas para buscar
     * @param password contraseña que se desea comprobar; puede ser nula
     * @return {@code true} si coincide; {@code false} si las entradas no son
     *         válidas, el usuario no existe o la verificación rechaza el hash
     * @throws SQLException si falla la consulta de usuarios
     * @throws IllegalStateException si falla la operación criptográfica
     */
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
