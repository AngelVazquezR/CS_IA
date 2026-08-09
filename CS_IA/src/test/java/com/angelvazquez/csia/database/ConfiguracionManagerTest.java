package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
