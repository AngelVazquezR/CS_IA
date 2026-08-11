package com.angelvazquez.csia.database.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.DatabaseType;

class ProfesorRepositoryTest {

    @TempDir
    Path tempDir;

    private ProfesorRepository repository;

    @BeforeEach
    void createRepository() {
        ConfigDB configuration = new ConfigDB();
        configuration.databaseType = DatabaseType.SQLITE;
        configuration.driver = "org.sqlite.JDBC";
        configuration.url = "jdbc:sqlite:"
                + tempDir.resolve("profesores.db");

        repository = new ProfesorRepository(
                new DatabaseConnectionFactory(),
                configuration
        );
    }

    @Test
    void completaElCrudDeProfesores() throws Exception {
        assertEquals(
                "PR0001",
                repository.agregar(
                        "Ana",
                        "Pérez",
                        "12345678A",
                        "2026-08-11",
                        ""
                )
        );
        assertTrue(repository.existeDni("12345678A"));
        assertEquals(1, repository.listar().size());

        assertEquals(
                1,
                repository.modificar(
                        "Ana María",
                        "Pérez",
                        "12345678A",
                        "2026-08-11",
                        "2026-12-01"
                )
        );
        assertEquals("Ana María", repository.listar().get(0).nombre());

        assertEquals(
                1,
                repository.eliminar(
                        "Ana María",
                        "Pérez",
                        "12345678A"
                )
        );
        assertFalse(repository.existeDni("12345678A"));
    }

    @Test
    void noReutilizaIdsTrasEliminarElUltimoRegistro() throws Exception {
        repository.agregar("Ana", "Uno", "11111111A", "", "");
        repository.agregar("Bea", "Dos", "22222222B", "", "");
        repository.eliminar("Ana", "Uno", "11111111A");

        assertEquals(
                "PR0003",
                repository.agregar("Carla", "Tres", "33333333C", "", "")
        );
    }

    @Test
    void trataComoDatoUnIntentoDeInyeccionSql() throws Exception {
        repository.agregar("Ana", "Pérez", "12345678A", "", "");

        assertFalse(repository.existeDni("' OR '1'='1"));
        assertEquals(
                0,
                repository.eliminar("Ana' OR '1'='1", "Pérez", "12345678A")
        );
        assertEquals(1, repository.listar().size());
    }
}
