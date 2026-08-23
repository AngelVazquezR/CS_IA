package com.angelvazquez.csia.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class I18nTest {

    @AfterEach
    void restaurarIdioma() {
        I18n.setIdioma(Idioma.INGLES);
    }

    @Test
    void cargaTraduccionEspanola() {
        I18n.setIdioma(Idioma.ESPANOL);
        assertEquals("Idioma", I18n.get("app.language"));
    }

    @Test
    void cargaTraduccionInglesa() {
        I18n.setIdioma(Idioma.INGLES);
        assertEquals("Language", I18n.get("app.language"));
    }

    @Test
    void sustituyeParametrosDinamicos() {
        I18n.setIdioma(Idioma.ESPANOL);
        assertEquals(
                "El motor de base de datos SQLite no está habilitado en esta versión.",
                I18n.get("database.engine.disabled", "SQLite"));
    }

    @Test
    void marcaLasClavesNoExistentesSinLanzarExcepcion() {
        assertEquals("!clave.inexistente!", I18n.get("clave.inexistente"));
    }
}
