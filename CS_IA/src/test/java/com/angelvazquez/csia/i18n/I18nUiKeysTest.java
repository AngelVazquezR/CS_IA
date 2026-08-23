package com.angelvazquez.csia.i18n;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class I18nUiKeysTest {

    private static final String[] CLAVES_CRITICAS = {
            "database.config.title",
            "database.validation.noEngine",
            "login.title",
            "login.invalidCredentials",
            "user.initial.title",
            "user.register.title",
            "home.title",
            "home.assignTeacher",
            "assign.title",
            "assign.peopleLoadError",
            "day.monday",
            "day.sunday",
            "table.name",
            "table.subject",
            "students.title",
            "students.deleteError",
            "teachers.title",
            "teachers.deleteError"
    };

    @AfterEach
    void restaurarIdioma() {
        I18n.setIdioma(Idioma.INGLES);
    }

    @Test
    void lasClavesCriticasExistenEnTodosLosIdiomas() {
        for (Idioma idioma : Idioma.values()) {
            I18n.setIdioma(idioma);
            for (String clave : CLAVES_CRITICAS) {
                String texto = I18n.get(clave, "detalle");
                assertFalse(texto.isBlank(),
                        () -> "Traducción vacía para " + clave + " en " + idioma.getCodigo());
            }
        }
    }
}
