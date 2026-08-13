package com.angelvazquez.csia.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Inicializa de forma idempotente el esquema SQLite v2. */
final class SqliteSchemaInitializer {

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

        validarEsquema(connection);
    }

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
