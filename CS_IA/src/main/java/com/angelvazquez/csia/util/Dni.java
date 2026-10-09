package com.angelvazquez.csia.util;

import java.util.Locale;
import com.angelvazquez.csia.i18n.I18n;

/** Normalización y validación de DNI/NIE */
public final class Dni {
    private Dni() { }

    /** Normaliza DNI/NIE sin validar su formato, para buscar también datos antiguos. */
    public static String normalizar(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException(I18n.get("person.dniRequired"));
        }
        return dni.trim().toUpperCase(Locale.ROOT);
    }
    /**
     * Normaliza y comprueba formato y letra de control antes de guardar.
     * DNI: ocho cifras y letra. NIE: X/Y/Z, siete cifras y letra.
     * Algoritmo: Ministerio del Interior, cálculo del dígito de control del NIF/NIE.
     * https://www.interior.gob.es/opencms/es/servicios-al-ciudadano/tramites-y-gestiones/dni/calculo-del-digito-de-control-del-nif-nie
     */
    public static String normalizarYValidar(String documento) {
        String valor = normalizar(documento);
        if (!valor.matches("(?:[0-9]{8}|[XYZ][0-9]{7})[A-Z]")) {
            throw new IllegalArgumentException(I18n.get("person.invalidDocument"));
        }
        String numero = valor.substring(0, 8);
        if (valor.charAt(0) == 'X' || valor.charAt(0) == 'Y' || valor.charAt(0) == 'Z') {
            numero = "XYZ".indexOf(valor.charAt(0)) + valor.substring(1, 8);
        }
        char letra = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(Integer.parseInt(numero) % 23);
        if (valor.charAt(8) != letra) {
            throw new IllegalArgumentException(I18n.get("person.invalidDocument"));
        }
        return valor;
    }
}
