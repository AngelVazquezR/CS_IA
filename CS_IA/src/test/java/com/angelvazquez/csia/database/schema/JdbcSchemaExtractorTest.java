package com.angelvazquez.csia.database.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

class JdbcSchemaExtractorTest {

    @Test
    void extraeElModeloMedianteMetadatosJdbc() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    CREATE TABLE PERSONAS (
                        ID VARCHAR(6) NOT NULL PRIMARY KEY,
                        NOMBRE VARCHAR(45),
                        UNIQUE (NOMBRE)
                    )
                    """);

            DatabaseSchema schema =
                    new JdbcSchemaExtractor().extract(connection);
            DatabaseSchema.Table table = schema.tables().get("PERSONAS");

            assertTrue(schema.tables().containsKey("PERSONAS"));
            assertEquals(java.util.List.of("ID", "NOMBRE"), table.columnOrder());
            assertFalse(table.columns().get("ID").nullable());
            assertTrue(table.columns().get("NOMBRE").nullable());
            assertEquals(java.util.List.of("ID"), table.primaryKey());
        }
    }
}
