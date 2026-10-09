package com.angelvazquez.csia.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/** Crea conexiones JDBC a partir de ConfigDB. */
public final class DatabaseConnectionFactory {

    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";
    // La caché es por archivo físico, no por instancia de la factoría: los
    // repositorios pueden crear factorías distintas para la misma base.
    private static final Object SCHEMA_LOCK = new Object();
    private static final Map<Path, Object> INITIALIZED_FILES = new HashMap<>();
    private static final SqliteSchemaInitializer SQLITE_INITIALIZER =
            new SqliteSchemaInitializer();

    /**
     * Abre una conexión y habilita sus claves foráneas SQLite. Inicializa el
     * esquema sólo en la primera apertura de cada archivo durante el proceso,
     * o cuando detecta que ese archivo ha sido sustituido. Las URI especiales
     * y las bases en memoria se inicializan en cada apertura.
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
            // Sólo se prepara el directorio cuando se necesita inicializar.
            Path databasePath = rutaSqliteConvencional(configuration.url);
            boolean requiresInitialization;
            synchronized (SCHEMA_LOCK) {
                requiresInitialization = databasePath == null
                        || !Files.isRegularFile(databasePath)
                        || !Objects.equals(INITIALIZED_FILES.get(databasePath),
                                identidadArchivo(databasePath));
                if (requiresInitialization) {
                    crearDirectorioSqlite(configuration.url);
                }
            }
            Connection connection = DriverManager.getConnection(configuration.url);

            try {
                try (Statement statement = connection.createStatement()) {
                    // La comprobación de claves foráneas se activa en cada conexión SQLite.
                    statement.execute("PRAGMA foreign_keys = ON");
                }
                // El bloqueo impide inicializaciones concurrentes duplicadas.
                // La identidad detecta una base borrada y recreada en la misma ruta.
                synchronized (SCHEMA_LOCK) {
                    if (databasePath == null
                            || !Objects.equals(INITIALIZED_FILES.get(databasePath),
                                    identidadArchivo(databasePath))) {
                        SQLITE_INITIALIZER.initialize(connection);
                        if (databasePath != null) {
                            INITIALIZED_FILES.put(databasePath, identidadArchivo(databasePath));
                        }
                    }
                }
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
     * Devuelve la ruta normalizada para una base SQLite de fichero convencional.
     * Las URI y bases en memoria no se almacenan en caché.
     */
    private Path rutaSqliteConvencional(String url) {
        if (url == null || !url.startsWith(SQLITE_URL_PREFIX)) return null;
        String location = url.substring(SQLITE_URL_PREFIX.length());
        int queryStart = location.indexOf('?');
        if (queryStart >= 0) return null;
        if (location.isBlank() || location.equals(":memory:")
                || location.startsWith("file:") || location.startsWith(":resource:")) {
            return null;
        }
        try {
            return Paths.get(location).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            return null;
        }
    }

    private Object identidadArchivo(Path path) throws SQLException {
        try {
            if (!Files.isRegularFile(path)) return null;
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            Object fileKey = attributes.fileKey();
            return fileKey != null ? fileKey : attributes.creationTime();
        } catch (IOException e) {
            throw new SQLException("No se pueden comprobar los atributos de SQLite: " + path, e);
        }
    }

    /**
     * Prepara el directorio de una URL SQLite con ruta de fichero convencional.
     * Las bases en memoria, las URI file: y los recursos quedan a cargo del driver.
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
