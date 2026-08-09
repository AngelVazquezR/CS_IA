package com.angelvazquez.csia.database;

import java.util.Locale;

public enum DatabaseType {

    MYSQL("mysql"),
    SQLITE("sqlite");

    private final String configValue;

    DatabaseType(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    public static DatabaseType fromConfigValue(String value) {

        if (value == null || value.isBlank()) {
            return MYSQL;
        }

        String normalizedValue =
                value.trim().toLowerCase(Locale.ROOT);

        for (DatabaseType type : values()) {
            if (type.configValue.equals(normalizedValue)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Tipo de base de datos no válido: '"
                        + value
                        + "'. Valores admitidos: mysql, sqlite."
        );
    }
}
