package com.angelvazquez.csia.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.i18n.Idioma;

class StartupManagerTest {

    @Test
    void configuracionNulaRequiereSeleccionDeIdioma() {
        assertTrue(StartupManager.requiereSeleccionIdioma(null));
    }

    @Test
    void configuracionLegadaSinIdiomaRequiereSeleccion() {
        AppConfig configuracion = new AppConfig(null, new ConfigDB());

        assertTrue(StartupManager.requiereSeleccionIdioma(configuracion));
    }

    @Test
    void configuracionConIdiomaNoVuelveAPreguntar() {
        AppConfig configuracion = new AppConfig(Idioma.ESPANOL, new ConfigDB());

        assertFalse(StartupManager.requiereSeleccionIdioma(configuracion));
    }
}
