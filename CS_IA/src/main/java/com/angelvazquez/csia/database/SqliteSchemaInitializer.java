package com.angelvazquez.csia.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Inicializa de forma idempotente el esquema SQLite v2. */
final class SqliteSchemaInitializer {

    /**
     * Crea las tablas ausentes sin borrar las existentes ni cerrar la conexión.
     * CREATE TABLE IF NOT EXISTS no migra columnas existentes; después instala
     * la unicidad de DNI y el rechazo de autoasignaciones mediante índices y triggers.
     *
     * @param connection conexión SQLite abierta, propiedad del llamador
     * @throws SQLException si falla el DDL o falta alguna de las tablas esperadas
     */
    void initialize(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS STUDENTS (
                        STUDENT_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        FIRST_NAME TEXT NOT NULL,
                        LAST_NAME TEXT NOT NULL,
                        DNI TEXT NOT NULL,
                        EMAIL TEXT NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS TEACHERS (
                        TEACHER_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        FIRST_NAME TEXT NOT NULL,
                        LAST_NAME TEXT NOT NULL,
                        DNI TEXT NOT NULL,
                        SUBJECT TEXT NOT NULL,
                        EMAIL TEXT NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS USERS (
                        USERNAME TEXT NOT NULL,
                        USER_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        PASSWORD_HASH TEXT NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS ASSIGNMENTS (
                        ASSIGNMENT_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        TEACHER_ID INTEGER,
                        STUDENT_ID INTEGER,
                        DAY_OF_WEEK INTEGER NOT NULL,
                        START_TIME TEXT NOT NULL,
                        START_DATE TEXT NOT NULL,
                        END_DATE TEXT NOT NULL,
                        CONSTRAINT ASSIGNMENTS_TEACHERS_FK
                            FOREIGN KEY (TEACHER_ID) REFERENCES TEACHERS(TEACHER_ID),
                        CONSTRAINT ASSIGNMENTS_STUDENTS_FK
                            FOREIGN KEY (STUDENT_ID) REFERENCES STUDENTS(STUDENT_ID)
                    )
                    """);
        }

        instalarIntegridadDni(connection);
        validarEsquema(connection);
    }

    /**
     * Instala índices y triggers idempotentes sin cambiar las columnas de ASSIGNMENTS.
     * Una base antigua con duplicados se rechaza; nunca se borran datos automáticamente.
     * El marcador separa sentencias completas, incluidos los cuerpos de los triggers.
     */
    private void instalarIntegridadDni(Connection connection) throws SQLException {
        try (InputStream input = SqliteSchemaInitializer.class.getResourceAsStream("/dni_integrity.sql")) {
            if (input == null) throw new SQLException("No se encuentra dni_integrity.sql.");
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement statement = connection.createStatement()) {
                for (String sentencia : sql.split("-- statement")) {
                    if (!sentencia.isBlank()) statement.executeUpdate(sentencia);
                }
            }
        } catch (IOException e) {
            throw new SQLException("No se pueden leer las restricciones de DNI.", e);
        }
    }

    // Comprueba la existencia de tablas; no valida columnas, restricciones ni versiones.
    private void validarEsquema(Connection connection) throws SQLException {
        String[] tablas = {"STUDENTS", "TEACHERS", "USERS", "ASSIGNMENTS"};

        for (String tabla : tablas) {
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery(
                         "SELECT name FROM sqlite_master "
                                 + "WHERE type='table' AND name='" + tabla + "'")) {
                if (!rows.next()) {
                    throw new SQLException(
                            "No se ha podido inicializar la tabla SQLite " + tabla + "."
                    );
                }
            }
        }
    }
}
