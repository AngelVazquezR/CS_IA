package com.angelvazquez.csia.database.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SchemaComparatorTest {

    private final MySqlSchemaScriptParser parser =
            new MySqlSchemaScriptParser();
    private final SchemaComparator comparator = new SchemaComparator();

    @Test
    void aceptaModelosEquivalentes() {
        DatabaseSchema schema = parser.parse(script("ID, NOMBRE"));

        assertTrue(comparator.compare(schema, schema).matches());
    }

    @Test
    void rechazaDiferenciasDeNulabilidad() {
        DatabaseSchema expected = parser.parse(script("ID, NOMBRE"));
        DatabaseSchema actual = parser.parse("""
                CREATE TABLE `personas` (
                  `ID` varchar(6) NOT NULL,
                  `NOMBRE` varchar(45) NOT NULL,
                  PRIMARY KEY (`ID`)
                );
                """);

        SchemaComparator.Result result =
                comparator.compare(expected, actual);
        assertFalse(result.matches());
        assertTrue(result.report().contains("permite NULL"));
    }

    @Test
    void rechazaUnOrdenDeColumnasDiferente() {
        DatabaseSchema expected = parser.parse(script("ID, NOMBRE"));
        DatabaseSchema actual = parser.parse("""
                CREATE TABLE `personas` (
                  `NOMBRE` varchar(45) DEFAULT NULL,
                  `ID` varchar(6) NOT NULL,
                  PRIMARY KEY (`ID`)
                );
                """);

        SchemaComparator.Result result =
                comparator.compare(expected, actual);
        assertFalse(result.matches());
        assertTrue(result.report().contains("orden de columnas"));
    }

    private String script(String ignoredOrder) {
        return """
                CREATE TABLE `personas` (
                  `ID` varchar(6) NOT NULL,
                  `NOMBRE` varchar(45) DEFAULT NULL,
                  PRIMARY KEY (`ID`)
                );
                """;
    }
}
