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
}
