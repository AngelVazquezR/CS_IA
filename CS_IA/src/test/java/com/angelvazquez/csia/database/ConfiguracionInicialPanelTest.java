package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JComboBox;

import org.junit.jupiter.api.Test;

class ConfiguracionInicialPanelTest {

    @Test
    void muestraMysqlConSusValoresPredeterminados() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        assertInstanceOf(JComboBox.class, panel.getComponent(1));
        assertEquals(
                ConfiguracionInicialPanel.DRIVER_MYSQL,
                panel.obtenerDriver()
        );
        assertEquals(
                ConfiguracionInicialPanel.URL_MYSQL,
                panel.obtenerUrl()
        );
        assertTrue(panel.estanHabilitadasLasCredenciales());
    }

    @Test
    void seleccionarSqliteRellenaLosValoresJdbc() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        panel.seleccionarTipo(DatabaseType.SQLITE);

        assertEquals(
                ConfiguracionInicialPanel.DRIVER_SQLITE,
                panel.obtenerDriver()
        );
        assertEquals(
                ConfiguracionInicialPanel.URL_SQLITE,
                panel.obtenerUrl()
        );
        assertFalse(panel.esUrlEditable());
        assertEquals(
                ConfiguracionInicialPanel.NOMBRE_DB_SQLITE,
                panel.obtenerNombreBaseDatos()
        );
        assertFalse(panel.estanHabilitadasLasCredenciales());
    }

    @Test
    void sqliteGeneraLaUrlDesdeElNombreYNoExigeCredenciales() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        panel.seleccionarTipo(DatabaseType.SQLITE);
        panel.establecerNombreSqlite("academia");

        assertNull(panel.validar());
        assertEquals("jdbc:sqlite:data/academia.db", panel.obtenerUrl());

        ConfigDB configuracion = panel.crearConfiguracion();

        assertEquals(DatabaseType.SQLITE, configuracion.databaseType);
        assertEquals("academia", configuracion.db);
        assertEquals("", configuracion.user);
        assertEquals("", configuracion.password);
    }

    @Test
    void sqliteNoDuplicaLaExtensionDb() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        panel.seleccionarTipo(DatabaseType.SQLITE);
        panel.establecerNombreSqlite("academia.DB");

        assertEquals("jdbc:sqlite:data/academia.DB", panel.obtenerUrl());
        assertNull(panel.validar());
    }

    @Test
    void sqliteExigeUnNombreDeBaseDeDatos() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        panel.seleccionarTipo(DatabaseType.SQLITE);
        panel.establecerNombreSqlite("   ");

        assertEquals(
                "Para SQLite, el nombre de la base de datos es obligatorio.",
                panel.validar()
        );
    }

    @Test
    void sqliteRechazaRutasEnElNombre() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        panel.seleccionarTipo(DatabaseType.SQLITE);
        panel.establecerNombreSqlite("otra/carpeta/academia");

        assertEquals(
                "El nombre de SQLite debe ser un nombre de fichero "
                        + "válido, sin rutas.",
                panel.validar()
        );
    }

    @Test
    void mysqlExigeBaseDeDatosYUsuario() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        assertEquals(
                "Para MySQL, la base de datos y el usuario "
                        + "son obligatorios.",
                panel.validar()
        );
    }

    @Test
    void mysqlPermitePasswordVacioComoHastaAhora() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        panel.establecerDatosMysql("CSIA", "root", "");

        assertNull(panel.validar());

        ConfigDB configuracion = panel.crearConfiguracion();

        assertEquals(DatabaseType.MYSQL, configuracion.databaseType);
        assertEquals("CSIA", configuracion.db);
        assertEquals("root", configuracion.user);
        assertEquals("", configuracion.password);
    }

    @Test
    void volverAMysqlRestauraValoresYCredenciales() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        panel.seleccionarTipo(DatabaseType.SQLITE);
        panel.seleccionarTipo(DatabaseType.MYSQL);

        assertEquals(
                ConfiguracionInicialPanel.DRIVER_MYSQL,
                panel.obtenerDriver()
        );
        assertEquals(
                ConfiguracionInicialPanel.URL_MYSQL,
                panel.obtenerUrl()
        );
        assertTrue(panel.esUrlEditable());
        assertTrue(panel.estanHabilitadasLasCredenciales());
    }
}
