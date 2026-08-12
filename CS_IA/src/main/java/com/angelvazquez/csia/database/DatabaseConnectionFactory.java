package com.angelvazquez.csia.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/** Crea conexiones JDBC a partir de ConfigDB. */
public final class DatabaseConnectionFactory {

    public Connection open(ConfigDB configuration) throws SQLException {
        Objects.requireNonNull(configuration, "La configuracion no puede ser null.");

        if (configuration.driver != null && !configuration.driver.isBlank()) {
            try {
                Class.forName(configuration.driver);
            } catch (ClassNotFoundException e) {
                throw new SQLException("No se ha encontrado el driver JDBC: " + configuration.driver, e);
            }
        }

        Connection connection;
        if (configuration.databaseType == DatabaseType.SQLITE) {
            connection = DriverManager.getConnection(configuration.url);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        }

        connection = DriverManager.getConnection(
                configuration.url,
                configuration.user,
                configuration.password
        );
        return connection;
    }
}
