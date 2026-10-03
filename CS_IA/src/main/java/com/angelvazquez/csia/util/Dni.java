package com.angelvazquez.csia.util;

import java.util.Locale;
import com.angelvazquez.csia.i18n.I18n;

/** Normalización compartida de DNI para persistencia y búsquedas; no valida su formato legal. */
public final class Dni {
    private Dni() { }

    /** Rechaza valores nulos o vacíos y devuelve el DNI recortado en mayúsculas. */
    public static String normalizar(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException(I18n.get("person.dniRequired"));
        }
        return dni.trim().toUpperCase(Locale.ROOT);
    }
}
