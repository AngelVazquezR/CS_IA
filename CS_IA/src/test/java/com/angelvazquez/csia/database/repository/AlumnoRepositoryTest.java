package com.angelvazquez.csia.database.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.DatabaseType;

class AlumnoRepositoryTest {

    @TempDir
    Path tempDir;

    private AlumnoRepository repository;

    @BeforeEach
    void createRepository() {
        ConfigDB configuration = new ConfigDB();
        configuration.databaseType = DatabaseType.SQLITE;
        configuration.driver = "org.sqlite.JDBC";
        configuration.url = "jdbc:sqlite:"
                + tempDir.resolve("alumnos.db");

        repository = new AlumnoRepository(
                new DatabaseConnectionFactory(),
                configuration
        );
    }

    @Test
    void completaElCrudYLaAsignacionDeProfesor() throws Exception {
        assertEquals(
                "AL0001",
                repository.agregar("Luis", "García", "12345678A")
        );
        assertTrue(repository.existeDni("12345678A"));
        assertNull(repository.listar().get(0).profesor());

        assertEquals(1, repository.asignarProfesor("ANA", "Luis"));
        assertEquals("ANA", repository.listar().get(0).profesor());

        assertEquals(
                1,
                repository.modificar("Luis Miguel", "García", "12345678A")
        );
        assertEquals("Luis Miguel", repository.listar().get(0).nombre());

        assertEquals(
                1,
                repository.eliminar(
                        "Luis Miguel",
                        "García",
                        "12345678A"
                )
        );
        assertFalse(repository.existeDni("12345678A"));
    }

    @Test
    void noReutilizaIdsTrasEliminarElUltimoRegistro() throws Exception {
        repository.agregar("Ana", "Uno", "11111111A");
        repository.agregar("Bea", "Dos", "22222222B");
        repository.eliminar("Ana", "Uno", "11111111A");

        assertEquals(
                "AL0003",
                repository.agregar("Carla", "Tres", "33333333C")
        );
    }

    @Test
    void trataComoDatoUnIntentoDeInyeccionSql() throws Exception {
        repository.agregar("Luis", "García", "12345678A");

        assertFalse(repository.existeDni("' OR '1'='1"));
        assertEquals(
                0,
                repository.eliminar("Luis' OR '1'='1", "García", "12345678A")
        );
        assertEquals(1, repository.listar().size());
    }
}
