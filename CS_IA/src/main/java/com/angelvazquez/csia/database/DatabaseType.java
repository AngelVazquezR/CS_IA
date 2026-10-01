package com.angelvazquez.csia.database;

import java.util.Arrays;
import java.util.Locale;

/**
 * Motores reconocidos por la configuración y su disponibilidad en esta versión.
 * MySQL se conserva en el modelo, pero su soporte v2 está aplazado; solo SQLite
 * está habilitado.
 */
public enum DatabaseType {
    MYSQL("mysql", false),
    SQLITE("sqlite", true);

    private final String configValue;
    private final boolean enabled;

    DatabaseType(String configValue, boolean enabled) {
        this.configValue = configValue;
        this.enabled = enabled;
    }

    public String getConfigValue() {
        return configValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Devuelve únicamente los motores que puede ofrecer el formulario de configuración. */
    public static DatabaseType[] enabledValues() {
        return Arrays.stream(values())
                .filter(DatabaseType::isEnabled)
                .toArray(DatabaseType[]::new);
    }

    /**
     * Interpreta el nombre del motor sin distinguir mayúsculas y minúsculas.
     * Conserva MYSQL como valor por defecto para entradas nulas o vacías.
     * Reconocer un motor no lo habilita: el llamador debe comprobar isEnabled().
     *
     * @param value nombre del motor guardado en la configuración
     * @return motor reconocido, aunque esté deshabilitado
     * @throws IllegalArgumentException si el nombre no corresponde a un motor conocido
     */
    public static DatabaseType fromConfigValue(String value) {
        if (value == null || value.isBlank()) {
            return MYSQL;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (DatabaseType type : values()) {
            if (type.configValue.equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Tipo de base de datos no valido: " + value);
    }
}
