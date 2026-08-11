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
        assertFalse(panel.estanHabilitadasLasCredenciales());
    }

    @Test
    void sqliteNoExigeBaseDeDatosNiCredenciales() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        panel.seleccionarTipo(DatabaseType.SQLITE);

        assertNull(panel.validar());

        ConfigDB configuracion = panel.crearConfiguracion();

        assertEquals(DatabaseType.SQLITE, configuracion.databaseType);
        assertEquals("", configuracion.db);
        assertEquals("", configuracion.user);
        assertEquals("", configuracion.password);
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
        assertTrue(panel.estanHabilitadasLasCredenciales());
    }
}
