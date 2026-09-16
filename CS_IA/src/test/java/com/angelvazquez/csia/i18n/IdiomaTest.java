package com.angelvazquez.csia.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class IdiomaTest {

    @Test
    void seleccionaEspanolCuandoElSistemaEstaEnEspanol() {
        assertEquals(Idioma.ESPANOL, Idioma.desdeLocale(Locale.forLanguageTag("es-ES")));
    }

    @Test
    void seleccionaInglesCuandoElSistemaEstaEnIngles() {
        assertEquals(Idioma.INGLES, Idioma.desdeLocale(Locale.forLanguageTag("en-GB")));
    }

    @Test
    void usaInglesCuandoElIdiomaDelSistemaNoEstaSoportado() {
        assertEquals(Idioma.INGLES, Idioma.desdeLocale(Locale.GERMAN));
        assertEquals(Idioma.INGLES, Idioma.desdeLocale(Locale.FRENCH));
    }

    @Test
    void resuelveCodigosSoportadosSinDistinguirMayusculas() {
        assertEquals(Idioma.ESPANOL, Idioma.desdeCodigo("ES").orElseThrow());
        assertEquals(Idioma.INGLES, Idioma.desdeCodigo("en").orElseThrow());
        assertTrue(Idioma.desdeCodigo("de").isEmpty());
    }
}
