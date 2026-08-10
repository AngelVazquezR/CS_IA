package com.angelvazquez.csia.database.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class SqliteSchemaScriptTest {

    @Test
    void creaElEsquemaEquivalenteYLaSegundaEjecucionConservaLosDatos()
            throws Exception {

        String script = loadSchema();

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite::memory:")) {

            executeScript(connection, script);

            assertEquals(
                    Set.of("ALUMNOS", "PROFESORES", "USUARIOS"),
                    tableNames(connection)
            );
            assertEquals(List.of(
                    new Column("ID", "VARCHAR(6)", false, false),
                    new Column("NOMBRE", "VARCHAR(45)", false, false),
                    new Column("APELLIDO", "VARCHAR(45)", false, false),
                    new Column("DNI", "VARCHAR(9)", false, false),
                    new Column("PROFESOR", "VARCHAR(20)", false, false)
            ), columns(connection, "ALUMNOS"));
            assertEquals(List.of(
                    new Column("ID", "VARCHAR(6)", true, true),
                    new Column("NOMBRE", "VARCHAR(45)", false, false),
                    new Column("APELLIDOS", "VARCHAR(45)", false, false),
                    new Column("DNI", "VARCHAR(9)", false, false),
                    new Column("FALTA", "VARCHAR(20)", false, false),
                    new Column("FBAJA", "VARCHAR(20)", false, false)
            ), columns(connection, "PROFESORES"));
            assertEquals(List.of(
                    new Column("USUARIO", "VARCHAR(45)", false, false),
                    new Column("CONTRASEÑA", "VARCHAR(45)", false, false)
            ), columns(connection, "USUARIOS"));
            assertTrue(hasUniqueIndex(
                    connection,
                    "USUARIOS",
                    "idx_USUARIOS_USUARIO"
            ));

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        INSERT INTO USUARIOS (USUARIO, CONTRASEÑA)
                        VALUES ('prueba', 'secreto')
                        """);
            }

            executeScript(connection, script);

            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery(
                         "SELECT COUNT(*) FROM USUARIOS")) {

                assertTrue(rows.next());
                assertEquals(1, rows.getInt(1));
            }
        }
    }

    private String loadSchema() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(
                "/schema-sqlite.sql")) {

            assertNotNull(
                    stream,
                    "schema-sqlite.sql debe estar incluido como recurso Maven."
            );
            return new String(
                    stream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }

    private void executeScript(Connection connection, String script)
            throws Exception {

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

    private Set<String> tableNames(Connection connection) throws Exception {
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

    private List<Column> columns(Connection connection, String table)
            throws Exception {

        List<Column> columns = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "PRAGMA table_info('" + table + "')")) {

            while (rows.next()) {
                columns.add(new Column(
                        rows.getString("name"),
                        rows.getString("type"),
                        rows.getInt("notnull") == 1,
                        rows.getInt("pk") > 0
                ));
            }
        }

        return columns;
    }

    private boolean hasUniqueIndex(
            Connection connection,
            String table,
            String indexName
    ) throws Exception {

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "PRAGMA index_list('" + table + "')")) {

            while (rows.next()) {
                if (indexName.equals(rows.getString("name"))
                        && rows.getInt("unique") == 1) {

                    return true;
                }
            }
        }

        return false;
    }

    private record Column(
            String name,
            String type,
            boolean notNull,
            boolean primaryKey
    ) {
    }
}
