package com.angelvazquez.csia.database.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MySqlSchemaScriptParserTest {

    private final MySqlSchemaScriptParser parser =
            new MySqlSchemaScriptParser();

    @Test
    void extraeColumnasClavePrimariaEIndiceUnico() {
        DatabaseSchema schema = parser.parse("""
                CREATE TABLE IF NOT EXISTS `profesores` (
                  `ID` varchar(6) NOT NULL,
                  `NOMBRE` varchar(45) DEFAULT NULL,
                  PRIMARY KEY (`ID`),
                  UNIQUE KEY `idx_nombre` (`NOMBRE`)
                ) ENGINE=InnoDB;
                """);

        DatabaseSchema.Table table = schema.tables().get("PROFESORES");
        assertEquals(2, table.columns().size());
        assertEquals("VARCHAR", table.columns().get("ID").type());
        assertEquals(6, table.columns().get("ID").size());
        assertFalse(table.columns().get("ID").nullable());
        assertTrue(table.columns().get("NOMBRE").nullable());
        assertEquals(java.util.List.of("ID", "NOMBRE"), table.columnOrder());
        assertEquals(java.util.List.of("ID"), table.primaryKey());
        assertEquals(1, table.indexes().size());
    }

    @Test
    void ignoraLasInstruccionesDeExportacionMysql() {
        DatabaseSchema schema = parser.parse("""
                /*!40101 SET NAMES utf8 */;
                DROP DATABASE IF EXISTS `csia`;
                CREATE DATABASE `csia`;
                USE `csia`;
                -- comentario
                CREATE TABLE `usuarios` (
                  `USUARIO` varchar(45) DEFAULT NULL
                ) ENGINE=InnoDB;
                """);

        assertEquals(1, schema.tables().size());
        assertTrue(schema.tables().containsKey("USUARIOS"));
    }
}
