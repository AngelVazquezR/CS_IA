package com.angelvazquez.csia.database.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.angelvazquez.csia.database.schema.JdbcSchemaExtractor.Extraction;

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

            Extraction extraction =
                    new JdbcSchemaExtractor().extractWithContext(connection);
            DatabaseSchema schema = extraction.schema();
            DatabaseSchema.Table table = schema.tables().get("PERSONAS");

            assertTrue(schema.tables().containsKey("PERSONAS"));
            assertEquals(List.of("ID", "NOMBRE"), table.columnOrder());
            assertFalse(table.columns().get("ID").nullable());
            assertTrue(table.columns().get("NOMBRE").nullable());
            assertEquals(List.of("ID"), table.primaryKey());

            String exported =
                    SchemaValidationMain.formatExtractedSchema(extraction);
            assertTrue(exported.contains("TABLA PERSONAS"));
            assertTrue(exported.contains("Tablas encontradas (1): PERSONAS"));
        }
    }

    @Test
    void resuelveElCatalogoSinDistinguirMayusculas() throws Exception {
        String selected = JdbcSchemaExtractor.chooseCatalog(
                "CSIA",
                List.of("information_schema", "csia", "mysql"));

        assertEquals("csia", selected);
    }
}
