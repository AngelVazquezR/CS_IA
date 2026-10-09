package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Comprueba que el esquema no se reinstala por cada conexión ordinaria. */
class SqliteConnectionInitializationTest {
    @TempDir Path temporary;

    private ConfigDB config(Path file) {
        ConfigDB config = new ConfigDB();
        config.databaseType = DatabaseType.SQLITE;
        config.driver = "org.sqlite.JDBC";
        config.url = "jdbc:sqlite:" + file.toAbsolutePath();
        return config;
    }

    @Test
    void conexionSinCambiosNoModificaVersionDelEsquema() throws Exception {
        ConfigDB config = config(temporary.resolve("database.db"));
        int version;
        try (Connection connection = new DatabaseConnectionFactory().open(config);
                Statement statement = connection.createStatement()) {
            assertTrue(exists(statement, "CSIA_ASSIGNMENTS_SELF_INSERT"));
            version = schemaVersion(statement);
        }

        try (Connection connection = new DatabaseConnectionFactory().open(config);
                Statement statement = connection.createStatement()) {
            assertTrue(exists(statement, "CSIA_ASSIGNMENTS_SELF_INSERT"));
            assertEquals(version, schemaVersion(statement));
            try (ResultSet result = statement.executeQuery("PRAGMA foreign_keys")) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
            }
        }
    }

    @Test
    void restauraUnTriggerSiElEsquemaHaCambiado() throws Exception {
        ConfigDB config = config(temporary.resolve("changed.db"));
        try (Connection connection = new DatabaseConnectionFactory().open(config);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TRIGGER CSIA_ASSIGNMENTS_SELF_INSERT");
            assertFalse(exists(statement, "CSIA_ASSIGNMENTS_SELF_INSERT"));
        }
        try (Connection connection = new DatabaseConnectionFactory().open(config);
                Statement statement = connection.createStatement()) {
            assertTrue(exists(statement, "CSIA_ASSIGNMENTS_SELF_INSERT"));
        }
    }

    private int schemaVersion(Statement statement) throws Exception {
        try (ResultSet result = statement.executeQuery("PRAGMA schema_version")) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    @Test
    void inicializaCadaArchivoDistinto() throws Exception {
        for (String name : new String[] {"one.db", "two.db"}) {
            try (Connection connection = new DatabaseConnectionFactory()
                    .open(config(temporary.resolve(name)));
                    Statement statement = connection.createStatement()) {
                assertTrue(exists(statement, "CSIA_ASSIGNMENTS_SELF_INSERT"));
            }
        }
    }

    private boolean exists(Statement statement, String trigger) throws Exception {
        try (ResultSet result = statement.executeQuery(
                "SELECT 1 FROM sqlite_master WHERE type='trigger' AND name='" + trigger + "'")) {
            return result.next();
        }
    }
}
