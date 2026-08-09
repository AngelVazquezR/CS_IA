package com.angelvazquez.csia.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Crea conexiones JDBC a partir de la configuración normalizada
 * de la aplicación.
 */
public final class DatabaseConnectionFactory {

    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";

    public Connection open(ConfigDB configuration)
            throws SQLException {

        Objects.requireNonNull(
                configuration,
                "La configuración de base de datos no puede ser null."
        );

        loadDriver(configuration.driver);

        if (configuration.databaseType == DatabaseType.SQLITE) {
            createSqliteDirectory(configuration.url);

            return DriverManager.getConnection(configuration.url);
        }

        return DriverManager.getConnection(
                configuration.url,
                configuration.user,
                configuration.password
        );
    }

    private void loadDriver(String driver) throws SQLException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "No se ha encontrado el driver JDBC: " + driver,
                    e
            );
        }
    }

    private void createSqliteDirectory(String url)
            throws SQLException {

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
            Path databasePath = Paths.get(location).toAbsolutePath();
            Path directory = databasePath.getParent();

            if (directory != null) {
                Files.createDirectories(directory);
            }
        } catch (IOException | InvalidPathException e) {
            throw new SQLException(
                    "No se ha podido crear el directorio para SQLite: "
                            + location,
                    e
            );
        }
    }
}
