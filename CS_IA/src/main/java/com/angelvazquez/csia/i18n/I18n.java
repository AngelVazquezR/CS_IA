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

    /**
     * Selecciona el idioma y carga sus textos para las consultas posteriores.
     * También cambia el Locale predeterminado de toda la JVM; no actualiza por sí
     * solo los textos que ya se hayan asignado a componentes Swing.
     *
     * @param idioma idioma no nulo que se desea utilizar
     * @throws NullPointerException si el idioma es nulo
     * @throws MissingResourceException si no se puede cargar el conjunto de recursos
     */
    public static synchronized void setIdioma(Idioma idioma) {
        idiomaActual = Objects.requireNonNull(idioma, "El idioma no puede ser null");
        bundle = cargar(idiomaActual);
        Locale.setDefault(idiomaActual.getLocale());
    }

    public static synchronized Idioma getIdioma() {
        return idiomaActual;
    }

    /**
     * Obtiene un texto por clave y, si hay parámetros, aplica MessageFormat con el idioma actual.
     * Si no hay parámetros, devuelve el patrón tal como está almacenado.
     *
     * @param key clave no nula del texto solicitado
     * @param parametros valores para los marcadores del patrón; puede ser nulo o estar vacío
     * @return texto obtenido o formateado; si falta la clave, devuelve !clave!
     * @throws NullPointerException si la clave es nula
     * @throws IllegalArgumentException si el patrón o sus parámetros no permiten el formato
     */
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

    /**
     * Delega en ResourceBundle la resolución del idioma y sus recursos de respaldo.
     */
    private static ResourceBundle cargar(Idioma idioma) {
        return ResourceBundle.getBundle(BUNDLE_BASE, idioma.getLocale());
    }
}
