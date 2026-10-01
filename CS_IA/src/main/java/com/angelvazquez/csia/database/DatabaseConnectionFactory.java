package com.angelvazquez.csia.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/** Crea conexiones JDBC a partir de ConfigDB. */
public final class DatabaseConnectionFactory {

    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";
    private static final SqliteSchemaInitializer SQLITE_INITIALIZER =
            new SqliteSchemaInitializer();

    /**
     * Abre una conexión y, para SQLite, crea el directorio si procede, habilita
     * claves foráneas e inicializa las tablas ausentes del esquema v2.
     *
     * @param configuration configuración no nula de un motor habilitado
     * @return conexión abierta; el llamador debe cerrarla, preferiblemente con
     *         try-with-resources
     * @throws SQLException si falta el motor, está deshabilitado o falla la
     *         preparación del directorio, la carga del driver o la operación JDBC
     * @throws NullPointerException si la configuración es nula
     */
    public Connection open(ConfigDB configuration) throws SQLException {
        Objects.requireNonNull(configuration, "La configuracion no puede ser null.");

        if (configuration.databaseType == null) {
            throw new SQLException("No se ha indicado el motor de base de datos.");
        }
        if (!configuration.databaseType.isEnabled()) {
            throw new SQLException(
                    "El motor de base de datos " + configuration.databaseType
                            + " no está habilitado en esta versión."
            );
        }

        if (configuration.driver != null && !configuration.driver.isBlank()) {
            try {
                Class.forName(configuration.driver);
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "No se ha encontrado el driver JDBC: " + configuration.driver,
                        e
                );
            }
        }

        if (configuration.databaseType == DatabaseType.SQLITE) {
            crearDirectorioSqlite(configuration.url);
            Connection connection = DriverManager.getConnection(configuration.url);

            try {
                try (Statement statement = connection.createStatement()) {
                    // La comprobación de claves foráneas se activa en cada conexión SQLite.
                    statement.execute("PRAGMA foreign_keys = ON");
                }
                SQLITE_INITIALIZER.initialize(connection);
                return connection;
            // Si la preparación falla, la conexión aún no se ha entregado al llamador.
            } catch (SQLException e) {
                try {
                    connection.close();
                } catch (SQLException closeFailure) {
                    e.addSuppressed(closeFailure);
                }
                throw e;
            }
        }

        return DriverManager.getConnection(
                configuration.url,
                configuration.user,
                configuration.password
        );
    }

    /**
     * Prepara el directorio de una URL SQLite con ruta de fichero convencional.
     * Las bases en memoria, las URI file: y los recursos quedan a cargo del driver;
     * la parte posterior a '?' no forma parte de la ruta que se crea.
     */
    private void crearDirectorioSqlite(String url) throws SQLException {
        if (url == null || !url.startsWith(SQLITE_URL_PREFIX)) {
            return;
        }

        String location = url.substring(SQLITE_URL_PREFIX.length());
        int queryStart = location.indexOf('?');
        if (queryStart >= 0) {
            location = location.substring(0, queryStart);
        }

        if (location.isBlank()
                || location.equals(":memory:")
                || location.startsWith("file:")
                || location.startsWith(":resource:")) {
            return;
        }

        try {
            Path databasePath = Paths.get(location).toAbsolutePath().normalize();
            Path directory = databasePath.getParent();
            if (directory != null) {
                Files.createDirectories(directory);
            }
        } catch (IOException | InvalidPathException e) {
            throw new SQLException(
                    "No se ha podido crear el directorio para SQLite: " + location,
                    e
            );
        }
    }
}
