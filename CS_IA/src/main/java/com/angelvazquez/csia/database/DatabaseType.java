package com.angelvazquez.csia.database;

import java.util.Arrays;
import java.util.Locale;

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

    public static DatabaseType[] enabledValues() {
        return Arrays.stream(values())
                .filter(DatabaseType::isEnabled)
                .toArray(DatabaseType[]::new);
    }

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
