package com.angelvazquez.csia.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/** Punto único de acceso a los textos internacionalizados de la aplicación. */
public final class I18n {
    private static final String BUNDLE_BASE = "i18n.messages";

    private static Idioma idiomaActual = Idioma.predeterminado();
    private static ResourceBundle bundle = cargar(idiomaActual);

    private I18n() {
    }

    public static synchronized void setIdioma(Idioma idioma) {
        idiomaActual = Objects.requireNonNull(idioma, "El idioma no puede ser null");
        bundle = cargar(idiomaActual);
        Locale.setDefault(idiomaActual.getLocale());
    }

    public static synchronized Idioma getIdioma() {
        return idiomaActual;
    }

    public static synchronized String get(String key, Object... parametros) {
        Objects.requireNonNull(key, "La clave de traducción no puede ser null");
        String patron;
        try {
            patron = bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }

        if (parametros == null || parametros.length == 0) {
            return patron;
        }
        MessageFormat formato = new MessageFormat(patron, idiomaActual.getLocale());
        return formato.format(parametros);
    }

    private static ResourceBundle cargar(Idioma idioma) {
        return ResourceBundle.getBundle(BUNDLE_BASE, idioma.getLocale());
    }
}
