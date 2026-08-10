package com.angelvazquez.csia.database.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class SqliteSchemaInitializerTest {

    @Test
    void inicializaUnaBaseNuevaEnUnaSolaTransaccion()
            throws Exception {

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite::memory:")) {

            new SqliteSchemaInitializer().initialize(connection);

            assertEquals(
                    Set.of("ALUMNOS", "PROFESORES", "USUARIOS"),
                    tableNames(connection)
            );
            assertEquals(1, schemaVersion(connection));
            assertTrue(connection.getAutoCommit());
        }
    }

    @Test
    void segundaInicializacionReutilizaElEsquemaYConservaDatos()
            throws Exception {

        AtomicInteger schemaLoads = new AtomicInteger();
        SqliteSchemaInitializer initializer = new SqliteSchemaInitializer(
                () -> {
                    schemaLoads.incrementAndGet();

                    return new ByteArrayInputStream(
                            "CREATE TABLE IF NOT EXISTS DATOS "
                                    .concat("(VALOR VARCHAR(20));")
                                    .getBytes(StandardCharsets.UTF_8)
                    );
                }
        );

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite::memory:")) {

            initializer.initialize(connection);

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                        "INSERT INTO DATOS (VALOR) VALUES ('persistente')"
                );
            }

            initializer.initialize(connection);

            assertEquals(1, schemaLoads.get());
            assertEquals(1, rowCount(connection, "DATOS"));
            assertEquals(1, schemaVersion(connection));
        }
    }

    @Test
    void errorEnElScriptRevierteTablasYVersion()
            throws Exception {

        String invalidScript = """
                CREATE TABLE TEMPORAL (ID INTEGER);
                SENTENCIA_SQL_INVALIDA;
                """;
        SqliteSchemaInitializer initializer = new SqliteSchemaInitializer(
                () -> new ByteArrayInputStream(
                        invalidScript.getBytes(StandardCharsets.UTF_8)
                )
        );

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite::memory:")) {

            assertThrows(
                    SQLException.class,
                    () -> initializer.initialize(connection)
            );

            assertFalse(tableNames(connection).contains("TEMPORAL"));
            assertEquals(0, schemaVersion(connection));
            assertTrue(connection.getAutoCommit());
        }
    }

    private Set<String> tableNames(Connection connection)
            throws SQLException {

        Set<String> names = new TreeSet<>();

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("""
                     SELECT name
                     FROM sqlite_master
                     WHERE type = 'table'
                       AND name NOT LIKE 'sqlite_%'
                     """)) {

            while (rows.next()) {
                names.add(rows.getString("name"));
            }
        }

        return names;
    }

    private int schemaVersion(Connection connection)
            throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "PRAGMA user_version")) {

            assertTrue(rows.next());

            return rows.getInt(1);
        }
    }

    private int rowCount(
            Connection connection,
            String table
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "SELECT COUNT(*) FROM " + table)) {

            assertTrue(rows.next());

            return rows.getInt(1);
        }
    }
}
