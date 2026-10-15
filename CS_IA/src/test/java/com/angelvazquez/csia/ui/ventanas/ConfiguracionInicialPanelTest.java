package com.angelvazquez.csia.ui.ventanas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseType;

class ConfiguracionInicialPanelTest {

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
        assertEquals(DatabaseType.SQLITE, configuracion.getDatabaseType());
        assertEquals("", configuracion.getUser());
        assertEquals("", configuracion.getPassword());
    }

    @Test
    void selectorSqliteNoDuplicaExtensionDb() {
        ConfiguracionInicialPanel panel = new ConfiguracionInicialPanel();
        panel.seleccionarTipo(DatabaseType.SQLITE);
        panel.establecerNombreSqlite("academia.db");

        assertEquals("jdbc:sqlite:data/academia.db", panel.obtenerUrl());
    }

}
