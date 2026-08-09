package com.angelvazquez.csia.database;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfigDBTest {

    @Test
    void nuevaConfiguracionMantieneLosValoresInicialesActuales() {
        ConfigDB configuracion = new ConfigDB();

        assertAll(
                () -> assertEquals("", configuracion.driver),
                () -> assertEquals("", configuracion.url),
                () -> assertEquals("", configuracion.db),
                () -> assertEquals("", configuracion.user),
                () -> assertEquals("", configuracion.password)
        );
    }
}
