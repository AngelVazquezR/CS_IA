package com.angelvazquez.csia.i18n;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** Idiomas soportados por la aplicación. */
public enum Idioma {
    ESPANOL("es", "Español"),
    INGLES("en", "English");

    private static final Idioma PREDETERMINADO = INGLES;

    private final String codigo;
    private final String nombre;

    Idioma(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public Locale getLocale() {
        return Locale.forLanguageTag(codigo);
    }

    public static Idioma predeterminado() {
        return PREDETERMINADO;
    }

    public static Idioma desdeSistema() {
        return desdeLocale(Locale.getDefault());
    }

    public static Idioma desdeLocale(Locale locale) {
        if (locale == null) {
            return PREDETERMINADO;
        }
        return desdeCodigo(locale.getLanguage()).orElse(PREDETERMINADO);
    }

    public static Optional<Idioma> desdeCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(idioma -> idioma.codigo.equalsIgnoreCase(codigo.trim()))
                .findFirst();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
