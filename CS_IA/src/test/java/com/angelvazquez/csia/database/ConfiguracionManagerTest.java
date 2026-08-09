package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfiguracionManagerTest {

    @TempDir
    Path tempDir;

    private final ConfiguracionManager manager =
            new ConfiguracionManager();

    @Test
    void leeTipoMysqlExplicito() throws Exception {
        ConfigDB configuracion =
                manager.leerConfiguracion(
                        crearConfiguracion("<tipo>mysql</tipo>")
                );

        assertEquals(
                DatabaseType.MYSQL,
                configuracion.databaseType
        );
    }

    @Test
    void leeTipoSqliteExplicito() throws Exception {
        ConfigDB configuracion =
                manager.leerConfiguracion(
                        crearConfiguracion("<tipo>sqlite</tipo>")
                );

        assertEquals(
                DatabaseType.SQLITE,
                configuracion.databaseType
        );
    }

    @Test
    void usaMysqlCuandoTipoNoExiste() throws Exception {
        ConfigDB configuracion =
                manager.leerConfiguracion(
                        crearConfiguracion("")
                );

        assertEquals(
                DatabaseType.MYSQL,
                configuracion.databaseType
        );
    }

    @Test
    void rechazaUnTipoDesconocido() throws Exception {
        IOException error = assertThrows(
                IOException.class,
                () -> manager.leerConfiguracion(
                        crearConfiguracion("<tipo>oracle</tipo>")
                )
        );

        assertTrue(
                error.getMessage().contains(
                        "Valores admitidos: mysql, sqlite"
                )
        );
    }

    @Test
    void permiteSqliteSinCredencialesNiNombreDeBaseDeDatos()
            throws Exception {

        ConfigDB configuracion = manager.leerConfiguracion(
                crearConfiguracionSqlite(
                        "<url>jdbc:sqlite:datos/pruebas.db</url>"
                )
        );

        assertEquals(DatabaseType.SQLITE, configuracion.databaseType);
        assertEquals("org.sqlite.JDBC", configuracion.driver);
        assertEquals(
                "jdbc:sqlite:datos/pruebas.db",
                configuracion.url
        );
        assertEquals("", configuracion.db);
        assertEquals("", configuracion.user);
        assertEquals("", configuracion.password);
    }

    @Test
    void usaLaUrlSqlitePredeterminadaCuandoNoSeIndica()
            throws Exception {

        ConfigDB configuracion = manager.leerConfiguracion(
                crearConfiguracionSqlite("")
        );

        assertEquals(
                "jdbc:sqlite:data/CSIA.db",
                configuracion.url
        );
    }

    @Test
    void mantieneLosCamposMysqlComoObligatorios()
            throws Exception {

        IOException error = assertThrows(
                IOException.class,
                () -> manager.leerConfiguracion(
                        crearConfiguracionMinima(
                                "<tipo>mysql</tipo>"
                        )
                )
        );

        assertTrue(
                error.getMessage().contains(
                        "Falta el elemento <driver>"
                )
        );
    }

    @Test
    void abreUnaBaseSqliteEnMemoria() throws Exception {
        try (Connection connection =
                     DriverManager.getConnection(
                             "jdbc:sqlite::memory:"
                     )) {

            assertFalse(connection.isClosed());
        }
    }

    private Path crearConfiguracionSqlite(String contenido)
            throws IOException {

        return crearConfiguracionMinima(
                "<tipo>sqlite</tipo>" + contenido
        );
    }

    private Path crearConfiguracionMinima(String contenido)
            throws IOException {

        Path ruta = tempDir.resolve(
                "configuracion-minima.xml"
        );

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <aplicacion>
                    <configuracion>
                        <baseDatos>
                            %s
                        </baseDatos>
                    </configuracion>
                </aplicacion>
                """.formatted(contenido);

        Files.writeString(ruta, xml);

        return ruta;
    }

    private Path crearConfiguracion(String tipo)
            throws IOException {

        Path ruta = tempDir.resolve("configuracion.xml");

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <aplicacion>
                    <configuracion>
                        <baseDatos>
                            %s
                            <driver>com.mysql.cj.jdbc.Driver</driver>
                            <url>jdbc:mysql://localhost:3306/CSIA</url>
                            <db>CSIA</db>
                            <usuario>root</usuario>
                            <password>secret</password>
                        </baseDatos>
                    </configuracion>
                </aplicacion>
                """.formatted(tipo);

        Files.writeString(ruta, xml);

        return ruta;
    }
}
