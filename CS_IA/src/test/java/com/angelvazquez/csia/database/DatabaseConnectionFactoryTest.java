package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseConnectionFactoryTest {

    @TempDir
    Path tempDir;

    private final DatabaseConnectionFactory connectionFactory =
            new DatabaseConnectionFactory();

    @Test
    void abreSqliteSinUsuarioNiPassword() throws Exception {
        ConfigDB configuration = sqliteConfiguration(
                "jdbc:sqlite::memory:"
        );

        try (Connection connection =
                     connectionFactory.open(configuration)) {

            assertFalse(connection.isClosed());
            assertTrue(tableExists(connection, "USUARIOS"));
        }
    }

    @Test
    void creaElDirectorioAntesDeAbrirSqlite() throws Exception {
        Path databasePath = tempDir
                .resolve("data")
                .resolve("CSIA.db");

        ConfigDB configuration = sqliteConfiguration(
                "jdbc:sqlite:" + databasePath
        );

        assertFalse(Files.exists(databasePath.getParent()));

        try (Connection connection =
                     connectionFactory.open(configuration)) {

            assertFalse(connection.isClosed());
        }

        assertTrue(Files.isDirectory(databasePath.getParent()));
        assertTrue(Files.isRegularFile(databasePath));
    }

    private boolean tableExists(
            Connection connection,
            String table
    ) throws Exception {

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("""
                     SELECT COUNT(*)
                     FROM sqlite_master
                     WHERE type = 'table'
                       AND name = '""" + table + "'")) {

            return rows.next() && rows.getInt(1) == 1;
        }
    }

    private ConfigDB sqliteConfiguration(String url) {
        ConfigDB configuration = new ConfigDB();
        configuration.databaseType = DatabaseType.SQLITE;
        configuration.driver = "org.sqlite.JDBC";
        configuration.url = url;

        return configuration;
    }
}
