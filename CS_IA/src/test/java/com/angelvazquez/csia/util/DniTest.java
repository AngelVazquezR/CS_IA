package com.angelvazquez.csia.util;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class DniTest {
    @Test void recortaYConvierteAMayusculasIndependientementeDelLocale() {
        Locale anterior = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));
            assertEquals("12345678I", Dni.normalizar(" 12345678i "));
        } finally { Locale.setDefault(anterior); }
    }
    @Test void rechazaNuloYVacioSinValidarFormatoLegal() {
        assertThrows(IllegalArgumentException.class, () -> Dni.normalizar(null));
        assertThrows(IllegalArgumentException.class, () -> Dni.normalizar(" "));
        assertEquals("ABC", Dni.normalizar("abc"));
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"12345678Z", "00000000T", "X1234567L", "Y1234567X", "Z1234567R"})
    void aceptaDocumentosConLetraCorrecta(String documento) {
        assertEquals(documento, Dni.normalizarYValidar(" " + documento.toLowerCase(Locale.ROOT) + " "));
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"12345678A", "X1234567A", "Y1234567A", "Z1234567A", "ABC", "1234567L", "123456789Z", "W1234567L", "12345678-Z", "1234 5678Z", "１２３４５６７８Z"})
    void rechazaFormatoOLetraIncorrectos(String documento) {
        assertThrows(IllegalArgumentException.class, () -> Dni.normalizarYValidar(documento));
    }
    @Test void validacionRechazaNuloYVacio() {
        assertThrows(IllegalArgumentException.class, () -> Dni.normalizarYValidar(null));
        assertThrows(IllegalArgumentException.class, () -> Dni.normalizarYValidar(" "));
    }
}
