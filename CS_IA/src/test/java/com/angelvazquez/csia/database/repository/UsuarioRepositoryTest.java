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

class UsuarioRepositoryTest {

    @TempDir
    Path tempDir;

    private UsuarioRepository repository;

    @BeforeEach
    void createRepository() {
        ConfigDB configuration = new ConfigDB();
        configuration.databaseType = DatabaseType.SQLITE;
        configuration.driver = "org.sqlite.JDBC";
        configuration.url = "jdbc:sqlite:"
                + tempDir.resolve("usuarios.db");

        repository = new UsuarioRepository(
                new DatabaseConnectionFactory(),
                configuration
        );
    }

    @Test
    void registraYRecuperaUnUsuario() throws Exception {
        assertTrue(repository.registrar("angel", "hash-seguro"));
        assertEquals(
                "hash-seguro",
                repository.recuperarPassword("angel")
        );
    }

    @Test
    void noDuplicaUsuariosSinDistinguirMayusculas() throws Exception {
        assertTrue(repository.registrar("angel", "primera"));
        assertFalse(repository.registrar("ANGEL", "segunda"));
        assertEquals(
                "primera",
                repository.recuperarPassword("AnGeL")
        );
    }

    @Test
    void trataComoDatoUnIntentoDeInyeccionSql() throws Exception {
        repository.registrar("ADMIN", "hash-admin");

        assertEquals(
                "",
                repository.recuperarPassword("ADMIN' OR '1'='1")
        );
    }

    @Test
    void conservaCaracteresUnicodeEnUsuarioYPassword() throws Exception {
        assertTrue(repository.registrar(
                "josé",
                "contraseña-Árbol-東京"
        ));
        assertEquals(
                "contraseña-Árbol-東京",
                repository.recuperarPassword("JOSÉ")
        );
    }
}
