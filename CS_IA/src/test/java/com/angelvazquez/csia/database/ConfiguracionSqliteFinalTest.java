package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.i18n.Idioma;

class ConfiguracionSqliteFinalTest {

    @TempDir
    Path temporal;

    @AfterEach
    void restaurarIdioma() {
        I18n.setIdioma(Idioma.INGLES);
    }

    @Test
    void configuracionMysqlExistenteSeRechazaMientrasEsteDeshabilitada()
            throws Exception {
        I18n.setIdioma(Idioma.ESPANOL);
        Path ruta = temporal.resolve("configuracion-mysql.xml");
        Files.writeString(ruta, """
                <configuracion>
                    <baseDatos>
                        <tipo>mysql</tipo>
                        <driver>com.mysql.cj.jdbc.Driver</driver>
                        <url>jdbc:mysql://localhost:3306/</url>
                        <usuario>usuario</usuario>
                        <password>clave</password>
                        <db>csia</db>
                    </baseDatos>
                </configuracion>
                """);

        IOException error = assertThrows(
                IOException.class,
                () -> new ConfiguracionManager(temporal).leerConfiguracion(ruta)
        );
        assertEquals(I18n.get("database.engine.disabled", DatabaseType.MYSQL),
                error.getMessage());
    }

    @Test
    void factoryRechazaMysqlAntesDeIntentarConectar() {
        ConfigDB configuracion = new ConfigDB();
        configuracion.setDatabaseType(DatabaseType.MYSQL);
        configuracion.setDriver("com.mysql.cj.jdbc.Driver");
        configuracion.setUrl("jdbc:mysql://localhost:3306/");
        configuracion.setDb("csia");
        configuracion.setUser("usuario");
        configuracion.setPassword("clave");

        SQLException error = assertThrows(
                SQLException.class,
                () -> new DatabaseConnectionFactory().open(configuracion)
        );
        assertTrue(error.getMessage().contains("no está habilitado"));
    }

    @Test
    void factoryCreaFicheroYEsquemaV2DesdeCero() throws Exception {
        Path database = temporal.resolve("datos").resolve("nueva.getDb()");
        ConfigDB configuracion = sqlite(database);

        try (Connection connection = new DatabaseConnectionFactory().open(configuracion)) {
            assertTrue(Files.isRegularFile(database));
            assertEquals(
                    Set.of("STUDENTS", "TEACHERS", "USERS", "ASSIGNMENTS"),
                    tablasV2(connection)
            );

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        INSERT INTO STUDENTS (FIRST_NAME, LAST_NAME, DNI, EMAIL)
                        VALUES ('Ana', 'Lopez', '123A', 'ana@example.com')
                        """);
            }
        }
    }

    @Test
    void reaperturaSqliteEsIdempotenteYConservaDatos() throws Exception {
        Path database = temporal.resolve("reapertura").resolve("academia.getDb()");
        ConfigDB configuracion = sqlite(database);
        DatabaseConnectionFactory factory = new DatabaseConnectionFactory();

        try (Connection connection = factory.open(configuracion);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO USERS (USERNAME, PASSWORD_HASH)
                    VALUES ('ADMIN', 'hash-de-prueba')
                    """);
        }

        try (Connection connection = factory.open(configuracion);
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "SELECT COUNT(*) FROM USERS WHERE USERNAME='ADMIN'")) {
            assertTrue(rows.next());
            assertEquals(1, rows.getInt(1));
        }
    }

    private ConfigDB sqlite(Path database) {
        ConfigDB configuracion = new ConfigDB();
        configuracion.setDatabaseType(DatabaseType.SQLITE);
        configuracion.setDriver("org.sqlite.JDBC");
        configuracion.setUrl("jdbc:sqlite:" + database.toAbsolutePath());
        configuracion.setDb(database.getFileName().toString());
        configuracion.setUser("");
        configuracion.setPassword("");
        return configuracion;
    }

    private Set<String> tablasV2(Connection connection) throws Exception {
        Set<String> tablas = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table'")) {
            while (rows.next()) {
                String nombre = rows.getString(1);
                if (!nombre.startsWith("sqlite_")) {
                    tablas.add(nombre);
                }
            }
        }
        return tablas;
    }
}