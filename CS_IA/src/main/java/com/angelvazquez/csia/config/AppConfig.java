package com.angelvazquez.csia.config;

import java.util.Objects;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.i18n.Idioma;

/** Configuración global de la aplicación. */
public final class AppConfig {

    private Idioma idioma;
    private ConfigDB database;

    public AppConfig() {
        this(null, new ConfigDB());
    }

    public AppConfig(Idioma idioma, ConfigDB database) {
        this.idioma = idioma;
        this.database = Objects.requireNonNull(database, "database");
    }

    public Idioma getIdioma() {
        return idioma;
    }

    public void setIdioma(Idioma idioma) {
        this.idioma = idioma;
    }

    public ConfigDB getDatabase() {
        return database;
    }

    public void setDatabase(ConfigDB database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    public boolean tieneIdiomaConfigurado() {
        return idioma != null;
    }
}
