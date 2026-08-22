package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfiguracionSqliteFinalTest {

    @TempDir
    Path temporal;

    @Test
    void selectorSoloMuestraMotoresHabilitados() {
        ConfiguracionInicialPanel panel = new ConfiguracionInicialPanel();

        assertTrue(DatabaseType.SQLITE.isEnabled());
        assertFalse(DatabaseType.MYSQL.isEnabled());
        assertEquals(1, panel.numeroTiposDisponibles());
        assertTrue(panel.contieneTipo(DatabaseType.SQLITE));
        assertFalse(panel.contieneTipo(DatabaseType.MYSQL));
    }

    @Test
    void selectorSqliteCompletaDriverUrlYCredencialesOpcionales() {
        ConfiguracionInicialPanel panel = new ConfiguracionInicialPanel();
        panel.seleccionarTipo(DatabaseType.SQLITE);
        panel.establecerNombreSqlite("academia");

        assertEquals("org.sqlite.JDBC", panel.obtenerDriver());
        assertEquals("jdbc:sqlite:data/academia.db", panel.obtenerUrl());
        assertFalse(panel.estanHabilitadasLasCredenciales());
        assertEquals(null, panel.validar());

        ConfigDB configuracion = panel.crearConfiguracion();
        assertEquals(DatabaseType.SQLITE, configuracion.databaseType);
        assertEquals("", configuracion.user);
        assertEquals("", configuracion.password);
    }

    @Test
    void selectorSqliteNoDuplicaExtensionDb() {
        ConfiguracionInicialPanel panel = new ConfiguracionInicialPanel();
        panel.seleccionarTipo(DatabaseType.SQLITE);
        panel.establecerNombreSqlite("academia.db");

        assertEquals("jdbc:sqlite:data/academia.db", panel.obtenerUrl());
    }

    @Test
    void configuracionMysqlExistenteSeRechazaMientrasEsteDeshabilitada()
            throws Exception {
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
        assertTrue(error.getMessage().contains("no está habilitado"));
    }

    @Test
    void factoryRechazaMysqlAntesDeIntentarConectar() {
        ConfigDB configuracion = new ConfigDB();
        configuracion.databaseType = DatabaseType.MYSQL;
        configuracion.driver = "com.mysql.cj.jdbc.Driver";
        configuracion.url = "jdbc:mysql://localhost:3306/";
        configuracion.db = "csia";
        configuracion.user = "usuario";
        configuracion.password = "clave";

        SQLException error = assertThrows(
                SQLException.class,
                () -> new DatabaseConnectionFactory().open(configuracion)
        );
        assertTrue(error.getMessage().contains("no está habilitado"));
    }

    @Test
    void factoryCreaFicheroYEsquemaV2DesdeCero() throws Exception {
        Path database = temporal.resolve("datos").resolve("nueva.db");
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
        Path database = temporal.resolve("reapertura").resolve("academia.db");
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
        configuracion.databaseType = DatabaseType.SQLITE;
        configuracion.driver = "org.sqlite.JDBC";
        configuracion.url = "jdbc:sqlite:" + database.toAbsolutePath();
        configuracion.db = database.getFileName().toString();
        configuracion.user = "";
        configuracion.password = "";
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
