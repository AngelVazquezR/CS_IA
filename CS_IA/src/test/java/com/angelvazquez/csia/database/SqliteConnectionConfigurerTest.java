package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

class SqliteConnectionConfigurerTest {

    private final SqliteConnectionConfigurer configurer =
            new SqliteConnectionConfigurer();

    @Test
    void activaClavesExternasEnLaConexion() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite::memory:")) {

            configurer.configure(connection);

            assertEquals(
                    1,
                    pragmaInteger(connection, "PRAGMA foreign_keys")
            );
        }
    }

    @Test
    void aceptaUnaBaseConIntegridadCorrecta() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {

            statement.execute(
                    "CREATE TABLE DATOS (ID INTEGER PRIMARY KEY)"
            );
            statement.execute(
                    "INSERT INTO DATOS (ID) VALUES (1)"
            );

            configurer.validateIntegrity(connection);

            try (ResultSet rows = statement.executeQuery(
                    "PRAGMA integrity_check")) {

                assertTrue(rows.next());
                assertEquals("ok", rows.getString(1));
            }
        }
    }

    @Test
    void rechazaUnaBaseConEsquemaCorrupto() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {

            statement.execute(
                    "CREATE TABLE DATOS (ID INTEGER PRIMARY KEY)"
            );
            statement.execute("PRAGMA writable_schema = ON");
            statement.executeUpdate("""
                    UPDATE sqlite_schema
                    SET sql = 'CREATE TABLE DATOS ('
                    WHERE name = 'DATOS'
                    """);
            statement.execute("PRAGMA writable_schema = OFF");
            statement.execute("PRAGMA schema_version = 2");

            assertThrows(
                    SQLException.class,
                    () -> configurer.validateIntegrity(connection)
            );
        }
    }

    private int pragmaInteger(
            Connection connection,
            String pragma
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(pragma)) {

            assertTrue(rows.next());

            return rows.getInt(1);
        }
    }
}
