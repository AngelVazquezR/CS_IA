package com.angelvazquez.csia.database.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;

/**
 * Acceso a datos de usuarios mediante SQL JDBC portable.
 */
public final class UsuarioRepository {

    private static final String FIND_PASSWORD = """
            SELECT CONTRASEÑA
            FROM USUARIOS
            WHERE USUARIO = ?
            """;

    private static final String USER_EXISTS = """
            SELECT COUNT(*) AS NUM_USUARIOS
            FROM USUARIOS
            WHERE USUARIO = ?
            """;

    private static final String INSERT_USER = """
            INSERT INTO USUARIOS (USUARIO, CONTRASEÑA)
            VALUES (?, ?)
            """;

    private final DatabaseConnectionFactory connectionFactory;
    private final ConfigDB configuration;

    public UsuarioRepository(
            DatabaseConnectionFactory connectionFactory,
            ConfigDB configuration
    ) {
        this.connectionFactory = Objects.requireNonNull(
                connectionFactory,
                "La fábrica de conexiones no puede ser null."
        );
        this.configuration = Objects.requireNonNull(
                configuration,
                "La configuración de base de datos no puede ser null."
        );
    }

    public String recuperarPassword(String usuario) throws SQLException {
        String normalizedUser = normalizeUser(usuario);

        try (Connection connection =
                     connectionFactory.open(configuration);
             PreparedStatement statement =
                     connection.prepareStatement(FIND_PASSWORD)) {

            statement.setString(1, normalizedUser);

            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    return rows.getString("CONTRASEÑA");
                }
            }
        }

        return "";
    }

    public boolean registrar(String usuario, String password)
            throws SQLException {

        String normalizedUser = normalizeUser(usuario);
        Objects.requireNonNull(
                password,
                "La contraseña no puede ser null."
        );

        try (Connection connection =
                     connectionFactory.open(configuration)) {

            if (exists(connection, normalizedUser)) {
                return false;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(INSERT_USER)) {

                statement.setString(1, normalizedUser);
                statement.setString(2, password);

                return statement.executeUpdate() == 1;
            }
        }
    }

    private boolean exists(
            Connection connection,
            String normalizedUser
    ) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(USER_EXISTS)) {

            statement.setString(1, normalizedUser);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next()
                        && rows.getInt("NUM_USUARIOS") > 0;
            }
        }
    }

    private String normalizeUser(String usuario) {
        return Objects.requireNonNull(
                usuario,
                "El usuario no puede ser null."
        ).toUpperCase(Locale.ROOT);
    }
}
