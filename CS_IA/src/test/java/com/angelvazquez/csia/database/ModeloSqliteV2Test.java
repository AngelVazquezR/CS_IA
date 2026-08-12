package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ModeloSqliteV2Test {

    @Test
    void esquemaV2SeCreaEnSqliteYContieneLasTablasEsperadas() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            ejecutarEsquema(connection);

            Set<String> tablas = new HashSet<>();
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table'")) {
                while (rows.next()) {
                    tablas.add(rows.getString("name"));
                }
            }

            assertTrue(tablas.contains("STUDENTS"));
            assertTrue(tablas.contains("TEACHERS"));
            assertTrue(tablas.contains("USERS"));
            assertTrue(tablas.contains("ASSIGNMENTS"));
        }
    }

    @Test
    void studentsYTeachersContienenLasColumnasDelModeloV2() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            ejecutarEsquema(connection);

            assertEquals(Set.of("STUDENT_ID", "FIRST_NAME", "LAST_NAME", "DNI", "EMAIL"),
                    columnas(connection, "STUDENTS"));
            assertEquals(Set.of("TEACHER_ID", "FIRST_NAME", "LAST_NAME", "DNI", "SUBJECT", "EMAIL"),
                    columnas(connection, "TEACHERS"));
        }
    }

    @Test
    void assignmentsReferenciaStudentsYTeachers() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            ejecutarEsquema(connection);

            Set<String> referencias = new HashSet<>();
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("PRAGMA foreign_key_list(ASSIGNMENTS)")) {
                while (rows.next()) {
                    referencias.add(rows.getString("table") + "." + rows.getString("to"));
                }
            }

            assertEquals(Set.of("STUDENTS.STUDENT_ID", "TEACHERS.TEACHER_ID"), referencias);
        }
    }

    private Set<String> columnas(Connection connection, String tabla) throws SQLException {
        Set<String> columnas = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("PRAGMA table_info(" + tabla + ")")) {
            while (rows.next()) {
                columnas.add(rows.getString("name"));
            }
        }
        return columnas;
    }

    private void ejecutarEsquema(Connection connection) throws IOException, SQLException {
        String sql;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("modelo_sqlite.sql")) {
            if (input == null) {
                throw new IOException("No se encuentra modelo_sqlite.sql en el classpath de pruebas.");
            }
            sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        try (Statement statement = connection.createStatement()) {
            for (String sentencia : sql.split(";")) {
                String limpia = sentencia.trim();
                if (!limpia.isEmpty()) {
                    statement.execute(limpia);
                }
            }
        }
    }
}
