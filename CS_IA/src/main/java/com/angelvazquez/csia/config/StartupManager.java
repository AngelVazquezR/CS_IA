package com.angelvazquez.csia.config;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.ConfiguracionManager;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.i18n.Idioma;
import com.angelvazquez.csia.i18n.IdiomaPanel;

/** Orquesta la configuración necesaria antes de iniciar la aplicación. */
public final class StartupManager {

    private final ConfiguracionManager configuracionManager;

    public StartupManager() {
        this(new ConfiguracionManager());
    }

    StartupManager(ConfiguracionManager configuracionManager) {
        this.configuracionManager = configuracionManager;
    }

    public AppConfig inicializar() {
        try {
            Path ruta = configuracionManager.obtenerRutaConfiguracion();

            if (Files.isRegularFile(ruta)) {
                AppConfig configuracion =
                        configuracionManager.leerConfiguracionAplicacion(ruta);
                return completarIdiomaSiEsNecesario(ruta, configuracion);
            }

            Idioma idioma = solicitarIdioma();
            if (idioma == null) {
                return null;
            }
            I18n.setIdioma(idioma);

            ConfigDB database = configuracionManager.inicializarConfiguracion();
            if (database == null) {
                return null;
            }

            AppConfig configuracion = new AppConfig(idioma, database);
            configuracionManager.guardarConfiguracionAplicacion(ruta, configuracion);
            return configuracion;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "No se ha podido inicializar la aplicación.\n\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }

    private AppConfig completarIdiomaSiEsNecesario(
            Path ruta, AppConfig configuracion) throws Exception {
        if (configuracion.tieneIdiomaConfigurado()) {
            I18n.setIdioma(configuracion.getIdioma());
            return configuracion;
        }

        Idioma idioma = solicitarIdioma();
        if (idioma == null) {
            return null;
        }

        configuracion.setIdioma(idioma);
        I18n.setIdioma(idioma);
        configuracionManager.guardarConfiguracionAplicacion(ruta, configuracion);
        return configuracion;
    }

    private Idioma solicitarIdioma() {
        IdiomaPanel panel = new IdiomaPanel();
        int resultado = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Seleccione idioma / Select language",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (resultado != JOptionPane.OK_OPTION) {
            return null;
        }
        return panel.getIdiomaSeleccionado();
    }

    static boolean requiereSeleccionIdioma(AppConfig configuracion) {
        return configuracion == null || !configuracion.tieneIdiomaConfigurado();
    }
}
