package com.angelvazquez.csia.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aplica las opciones obligatorias de cada conexión SQLite y comprueba
 * que el fichero abierto no presenta errores de integridad.
 */
final class SqliteConnectionConfigurer {

    void configure(Connection connection) throws SQLException {
        Objects.requireNonNull(
                connection,
                "La conexión SQLite no puede ser null."
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "PRAGMA foreign_keys")) {

            if (!rows.next() || rows.getInt(1) != 1) {
                throw new SQLException(
                        "SQLite no ha activado las claves externas "
                                + "para la conexión."
                );
            }
        }
    }

    void validateIntegrity(Connection connection) throws SQLException {
        Objects.requireNonNull(
                connection,
                "La conexión SQLite no puede ser null."
        );

        List<String> errors = new ArrayList<>();
        boolean returnedRows = false;

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "PRAGMA integrity_check")) {

            while (rows.next()) {
                returnedRows = true;
                String result = rows.getString(1);

                if (!"ok".equalsIgnoreCase(result)) {
                    errors.add(result);
                }
            }
        }

        if (!returnedRows) {
            throw new SQLException(
                    "SQLite no ha devuelto el resultado de "
                            + "PRAGMA integrity_check."
            );
        }

        if (!errors.isEmpty()) {
            throw new SQLException(
                    "La comprobación de integridad de SQLite ha fallado: "
                            + String.join("; ", errors)
            );
        }
    }
}
