package com.angelvazquez.csia.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdiomaPanelTest {

    private Locale localeOriginal;

    @BeforeEach
    void guardarLocaleOriginal() {
        localeOriginal = Locale.getDefault();
    }

    @AfterEach
    void restaurarLocaleOriginal() {
        Locale.setDefault(localeOriginal);
        I18n.setIdioma(Idioma.predeterminado());
    }

    @Test
    void muestraTodosLosIdiomasSoportados() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            IdiomaPanel panel = new IdiomaPanel();
            assertEquals(Idioma.values().length, panel.getNumeroIdiomasDisponibles());
        });
    }

    @Test
    void preseleccionaIdiomaDelSistemaSiEstaSoportado() throws Exception {
        Locale.setDefault(Locale.forLanguageTag("es-ES"));

        SwingUtilities.invokeAndWait(() -> {
            IdiomaPanel panel = new IdiomaPanel();
            assertEquals(Idioma.ESPANOL, panel.getIdiomaSeleccionado());
        });
    }

    @Test
    void usaInglesSiIdiomaDelSistemaNoEstaSoportado() throws Exception {
        Locale.setDefault(Locale.GERMAN);

        SwingUtilities.invokeAndWait(() -> {
            IdiomaPanel panel = new IdiomaPanel();
            assertEquals(Idioma.INGLES, panel.getIdiomaSeleccionado());
        });
    }

    @Test
    void permiteSeleccionarIdiomaSinModificarI18nGlobal() throws Exception {
        I18n.setIdioma(Idioma.INGLES);

        SwingUtilities.invokeAndWait(() -> {
            IdiomaPanel panel = new IdiomaPanel();
            panel.setIdiomaSeleccionado(Idioma.ESPANOL);

            assertEquals(Idioma.ESPANOL, panel.getIdiomaSeleccionado());
            assertEquals(Idioma.INGLES, I18n.getIdioma());
        });
    }
}
