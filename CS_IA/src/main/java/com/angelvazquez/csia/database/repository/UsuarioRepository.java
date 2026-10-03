package com.angelvazquez.csia.database.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.model.Usuario;

/** Persistencia de usuarios sobre la tabla USERS. */
public final class UsuarioRepository {

    private static final String FIND_BY_USERNAME = """
            SELECT USER_ID, USERNAME, PASSWORD_HASH
            FROM USERS
            WHERE USERNAME = ?
            """;

    private static final String EXISTS_ANY = """
            SELECT 1
            FROM USERS
            LIMIT 1
            """;

    private static final String INSERT = """
            INSERT INTO USERS (USERNAME, PASSWORD_HASH)
            VALUES (?, ?)
            """;

    /**
     * Cada consulta o escritura abre y cierra su propia conexión.
     * La comprobación de usuario existente y la inserción no comparten una transacción.
     */
    private final DatabaseConnectionFactory connectionFactory;
    private final ConfigDB configuration;

    public UsuarioRepository(DatabaseConnectionFactory connectionFactory, ConfigDB configuration) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
        this.configuration = Objects.requireNonNull(configuration);
    }

    /**
     * Busca por nombre recortado y convertido a mayúsculas con Locale.ROOT.
     *
     * @param username nombre no nulo; aquí no se rechazan los nombres en blanco
     * @return primer usuario encontrado, o un Optional vacío si no existe
     * @throws NullPointerException si el nombre es nulo
     * @throws SQLException si falla la consulta
     */
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        String normalized = normalize(username);
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME)) {
            statement.setString(1, normalized);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Usuario(
                        rows.getInt("USER_ID"),
                        rows.getString("USERNAME"),
                        rows.getString("PASSWORD_HASH")
                ));
            }
        }
    }

    /**
     * Permite decidir si es necesario solicitar el primer usuario de la instalación.
     *
     * @return true si USERS contiene al menos una fila
     * @throws SQLException si falla la consulta
     */
    public boolean existeAlgunUsuario() throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(EXISTS_ANY);
             ResultSet rows = statement.executeQuery()) {
            return rows.next();
        }
    }

    /**
     * Comprueba si el nombre normalizado ya existe y después inserta el usuario.
     * Al obtener la clave, actualiza el ID y el nombre normalizado en la instancia recibida.
     * El hash debe llegar generado: este repositorio no valida su formato ni cifra contraseñas.
     *
     * @param usuario entidad no nula con nombre y hash no nulos
     * @return USER_ID generado
     * @throws NullPointerException si la entidad, el nombre o el hash son nulos
     * @throws IllegalArgumentException si la consulta previa encuentra el nombre
     * @throws SQLException si falla la consulta, la inserción o la obtención de la clave
     */
    public int registrar(Usuario usuario) throws SQLException {
        Objects.requireNonNull(usuario);
        String normalized = normalize(usuario.getUsername());
        // Esta consulta previa no garantiza unicidad ante registros concurrentes.
        if (buscarPorUsername(normalized).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con USERNAME " + normalized + ".");
        }

        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, normalized);
            statement.setString(2, Objects.requireNonNull(usuario.getPasswordHash(), "PASSWORD_HASH no puede ser null."));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se ha obtenido USER_ID al registrar el usuario.");
                }
                int id = keys.getInt(1);
                usuario.setId(id);
                usuario.setUsername(normalized);
                return id;
            }
        }
    }

    /**
     * Comparte la normalización entre búsqueda y registro, independiente del idioma de la JVM.
     */
    private String normalize(String username) {
        return Objects.requireNonNull(username, "USERNAME no puede ser null.")
                .trim().toUpperCase(Locale.ROOT);
    }
}
