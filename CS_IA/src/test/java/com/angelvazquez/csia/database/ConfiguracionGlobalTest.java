package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.angelvazquez.csia.config.AppConfig;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.i18n.Idioma;

class ConfiguracionGlobalTest {

    @TempDir
    Path temporal;

    @AfterEach
    void restaurarIdioma() {
        I18n.setIdioma(Idioma.INGLES);
    }

    @Test
    void leeConfiguracionGlobalConIdioma() throws Exception {
        Path ruta = temporal.resolve("configuracion.xml");
        Files.writeString(ruta, xml("es"));

        AppConfig config = new ConfiguracionManager(temporal)
                .leerConfiguracionAplicacion(ruta);

        assertTrue(config.tieneIdiomaConfigurado());
        assertEquals(Idioma.ESPANOL, config.getIdioma());
        assertEquals(DatabaseType.SQLITE, config.getDatabase().getDatabaseType());
        assertEquals("academia", config.getDatabase().getDb());
    }

    @Test
    void configuracionAntiguaSinIdiomaSigueSiendoValida() throws Exception {
        Path ruta = temporal.resolve("configuracion-antigua.xml");
        Files.writeString(ruta, """
                <configuracion>
                    <baseDatos>
                        <tipo>sqlite</tipo>
                        <driver>org.sqlite.JDBC</driver>
                        <url>jdbc:sqlite:data/academia.getDb()</url>
                        <usuario></usuario>
                        <password></password>
                        <db>academia</db>
                    </baseDatos>
                </configuracion>
                """);

        AppConfig config = new ConfiguracionManager(temporal)
                .leerConfiguracionAplicacion(ruta);

        assertFalse(config.tieneIdiomaConfigurado());
        assertEquals(null, config.getIdioma());
        assertEquals(DatabaseType.SQLITE, config.getDatabase().getDatabaseType());
    }

    @Test
    void rechazaIdiomaConfiguradoNoSoportado() throws Exception {
        I18n.setIdioma(Idioma.ESPANOL);
        Path ruta = temporal.resolve("configuracion-fr.xml");
        Files.writeString(ruta, xml("fr"));

        IOException error = assertThrows(
                IOException.class,
                () -> new ConfiguracionManager(temporal)
                        .leerConfiguracionAplicacion(ruta)
        );

        assertEquals(I18n.get("config.unsupportedLanguage", "fr"), error.getMessage());
    }

    @Test
    void guardaIdiomaYBaseDatosEnUnUnicoXml() throws Exception {
        Path ruta = temporal.resolve("config").resolve("configuracion.xml");
        ConfigDB database = sqlite();
        AppConfig original = new AppConfig(Idioma.INGLES, database);
        ConfiguracionManager manager = new ConfiguracionManager(temporal);

        manager.guardarConfiguracionAplicacion(ruta, original);

        String contenido = Files.readString(ruta);
        assertTrue(contenido.contains("<aplicacion>"));
        assertTrue(contenido.contains("<idioma>en</idioma>"));
        assertTrue(contenido.contains("<baseDatos>"));

        AppConfig recuperada = manager.leerConfiguracionAplicacion(ruta);
        assertEquals(Idioma.INGLES, recuperada.getIdioma());
        assertEquals(DatabaseType.SQLITE, recuperada.getDatabase().getDatabaseType());
        assertEquals("jdbc:sqlite:data/academia.getDb()", recuperada.getDatabase().getUrl());
    }

    @Test
    void noPermiteGuardarConfiguracionGlobalSinIdioma() {
        I18n.setIdioma(Idioma.ESPANOL);
        Path ruta = temporal.resolve("configuracion.xml");
        AppConfig config = new AppConfig(null, sqlite());

        IOException error = assertThrows(
                IOException.class,
                () -> new ConfiguracionManager(temporal)
                        .guardarConfiguracionAplicacion(ruta, config)
        );

        assertEquals(I18n.get("config.languageRequired"), error.getMessage());
    }

    private ConfigDB sqlite() {
        ConfigDB database = new ConfigDB();
        database.setDatabaseType(DatabaseType.SQLITE);
        database.setDriver("org.sqlite.JDBC");
        database.setUrl("jdbc:sqlite:data/academia.getDb()");
        database.setDb("academia");
        database.setUser("");
        database.setPassword("");
        return database;
    }

    private String xml(String idioma) {
        return """
                <configuracion>
                    <aplicacion>
                        <idioma>%s</idioma>
                    </aplicacion>
                    <baseDatos>
                        <tipo>sqlite</tipo>
                        <driver>org.sqlite.JDBC</driver>
                        <url>jdbc:sqlite:data/academia.getDb()</url>
                        <usuario></usuario>
                        <password></password>
                        <db>academia</db>
                    </baseDatos>
                </configuracion>
                """.formatted(idioma);
    }
}
