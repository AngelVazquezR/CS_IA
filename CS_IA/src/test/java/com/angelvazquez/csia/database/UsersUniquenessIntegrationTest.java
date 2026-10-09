package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifica la unicidad de USERNAME también en bases SQLite existentes. */
class UsersUniquenessIntegrationTest {
    @TempDir Path temporal;

    private ConfigDB config(Path file) {
        ConfigDB config = new ConfigDB();
        config.databaseType = DatabaseType.SQLITE;
        config.driver = "org.sqlite.JDBC";
        config.url = "jdbc:sqlite:" + file.toAbsolutePath();
        return config;
    }

    @Test
    void rechazaDuplicadosInclusoMedianteSqlDirecto() throws Exception {
        ConfigDB config = config(temporal.resolve("users.db"));
        try (Connection connection = new DatabaseConnectionFactory().open(config);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "INSERT INTO USERS (USERNAME, PASSWORD_HASH) VALUES ('ANGEL','hash')");
            assertThrows(SQLException.class, () -> statement.executeUpdate(
                    "INSERT INTO USERS (USERNAME, PASSWORD_HASH) VALUES (' angel ','otro')"));
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM USERS")) {
                rows.next();
                assertEquals(1, rows.getInt(1));
            }
        }
    }

    @Test
    void aplicaUnicidadAlAbrirBaseExistente() throws Exception {
        Path file = temporal.resolve("existing.db");
        ConfigDB config = config(file);
        try (Connection connection = DriverManager.getConnection(config.url);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE USERS (USERNAME TEXT NOT NULL,
                    USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, PASSWORD_HASH TEXT NOT NULL)
                    """);
            statement.executeUpdate(
                    "INSERT INTO USERS (USERNAME, PASSWORD_HASH) VALUES ('ADMIN','hash')");
        }
        try (Connection connection = new DatabaseConnectionFactory().open(config);
                Statement statement = connection.createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeUpdate(
                    "INSERT INTO USERS (USERNAME, PASSWORD_HASH) VALUES (' admin ','otro')"));
        }
    }

    @Test
    void rechazaBaseAntiguaConDuplicadosSinBorrarDatos() throws Exception {
        Path file = temporal.resolve("duplicates.db");
        ConfigDB config = config(file);
        try (Connection connection = DriverManager.getConnection(config.url);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE USERS (USERNAME TEXT NOT NULL,
                    USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, PASSWORD_HASH TEXT NOT NULL)
                    """);
            statement.executeUpdate(
                    "INSERT INTO USERS (USERNAME, PASSWORD_HASH) VALUES ('ADMIN','hash')");
            statement.executeUpdate(
                    "INSERT INTO USERS (USERNAME, PASSWORD_HASH) VALUES (' admin ','hash2')");
        }
        assertThrows(SQLException.class, () -> new DatabaseConnectionFactory().open(config));
        try (Connection connection = DriverManager.getConnection(config.url);
                Statement statement = connection.createStatement();
                var rows = statement.executeQuery("SELECT COUNT(*) FROM USERS")) {
            rows.next();
            assertEquals(2, rows.getInt(1));
        }
    }
}
