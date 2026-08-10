package com.angelvazquez.csia.database.schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Inicializa una base SQLite nueva usando el esquema empaquetado.
 *
 * La versión se guarda en PRAGMA user_version para evitar tablas técnicas
 * adicionales. El esquema y la marca de versión se confirman en la misma
 * transacción.
 */
public final class SqliteSchemaInitializer {

    static final int SCHEMA_VERSION = 1;

    private static final String SCHEMA_RESOURCE = "/schema-sqlite.sql";

    private final Supplier<InputStream> schemaSupplier;

    public SqliteSchemaInitializer() {
        this(() -> SqliteSchemaInitializer.class.getResourceAsStream(
                SCHEMA_RESOURCE
        ));
    }

    SqliteSchemaInitializer(Supplier<InputStream> schemaSupplier) {
        this.schemaSupplier = Objects.requireNonNull(
                schemaSupplier,
                "El proveedor del esquema SQLite no puede ser null."
        );
    }

    public void initialize(Connection connection) throws SQLException {
        Objects.requireNonNull(
                connection,
                "La conexión SQLite no puede ser null."
        );

        if (schemaVersion(connection) >= SCHEMA_VERSION) {
            return;
        }

        boolean previousAutoCommit = connection.getAutoCommit();

        if (!previousAutoCommit) {
            throw new SQLException(
                    "La inicialización de SQLite requiere una conexión "
                            + "sin una transacción activa."
            );
        }

        SQLException failure = null;
        connection.setAutoCommit(false);

        try {
            executeScript(connection, readSchema());

            try (Statement statement = connection.createStatement()) {
                statement.execute(
                        "PRAGMA user_version = " + SCHEMA_VERSION
                );
            }

            connection.commit();
        } catch (SQLException e) {
            failure = e;
            rollback(connection, e);

            throw e;
        } finally {
            try {
                connection.setAutoCommit(previousAutoCommit);
            } catch (SQLException e) {
                if (failure != null) {
                    failure.addSuppressed(e);
                } else {
                    throw e;
                }
            }
        }
    }

    private int schemaVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "PRAGMA user_version")) {

            if (!rows.next()) {
                throw new SQLException(
                        "SQLite no ha devuelto PRAGMA user_version."
                );
            }

            return rows.getInt(1);
        }
    }

    private String readSchema() throws SQLException {
        try (InputStream stream = schemaSupplier.get()) {
            if (stream == null) {
                throw new SQLException(
                        "No se ha encontrado el recurso "
                                + SCHEMA_RESOURCE + "."
                );
            }

            return new String(
                    stream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new SQLException(
                    "No se ha podido leer el esquema SQLite.",
                    e
            );
        }
    }

    private void executeScript(
            Connection connection,
            String script
    ) throws SQLException {

        String sqlWithoutComments = script.lines()
                .filter(line -> !line.stripLeading().startsWith("--"))
                .collect(Collectors.joining(System.lineSeparator()));

        try (Statement statement = connection.createStatement()) {
            for (String sql : sqlWithoutComments.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }

    private void rollback(
            Connection connection,
            SQLException failure
    ) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }
}
