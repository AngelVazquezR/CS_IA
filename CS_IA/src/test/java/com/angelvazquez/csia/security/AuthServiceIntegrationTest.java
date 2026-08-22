package com.angelvazquez.csia.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.DatabaseType;
import com.angelvazquez.csia.database.repository.UsuarioRepository;

class AuthServiceIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void registraYAutenticaUsuarioConPasswordHash() throws Exception {
        AuthService authService = crearServicio();

        authService.registrar("angel", "Password123".toCharArray());

        assertTrue(authService.autenticar("ANGEL", "Password123".toCharArray()));
        assertFalse(authService.autenticar("ANGEL", "Incorrecta123".toCharArray()));
    }

    @Test
    void rechazaUsuarioInexistente() throws Exception {
        AuthService authService = crearServicio();
        assertFalse(authService.autenticar("NO_EXISTE", "Password123".toCharArray()));
    }

    private AuthService crearServicio() throws Exception {
        Path db = tempDir.resolve("auth.db");
        String url = "jdbc:sqlite:" + db.toAbsolutePath();

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE USERS (
                        USERNAME TEXT NOT NULL,
                        USER_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        PASSWORD_HASH TEXT NOT NULL
                    )
                    """);
        }

        ConfigDB configuration = new ConfigDB();
        configuration.databaseType = DatabaseType.SQLITE;
        configuration.driver = "org.sqlite.JDBC";
        configuration.url = url;
        configuration.db = db.getFileName().toString();

        UsuarioRepository repository = new UsuarioRepository(
                new DatabaseConnectionFactory(), configuration);
        return new AuthService(repository, new PasswordHasher());
    }
}
