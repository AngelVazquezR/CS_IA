package com.angelvazquez.csia.ui.ventanas;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import com.angelvazquez.csia.Main;
import com.angelvazquez.csia.config.AppConfig;
import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.ConfiguracionInicialPanel;
import com.angelvazquez.csia.database.ConfiguracionManager;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.i18n.IdiomaPanel;

/** Preferencias persistentes de idioma y base de datos. */
public final class PreferenciasPage extends VentanaSecundaria {
    private static final long serialVersionUID = 1L;

    private final IdiomaPanel idiomaPanel = new IdiomaPanel();
    private final ConfiguracionInicialPanel databasePanel = new ConfiguracionInicialPanel();
    private final JButton guardarButton = new JButton(I18n.get("app.save"));
    private final JButton cancelarButton = new JButton(I18n.get("app.cancel"));

    public PreferenciasPage(Window parent) {
        super(parent);
        setTitle(I18n.get("preferences.title"));
        setBounds(100, 100, 620, 430);
        construirInterfaz();
        cargarConfiguracionActual();
        configurarAcciones();
    }

    private void construirInterfaz() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(contentPane);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(I18n.get("preferences.languageTab"), idiomaPanel);
        tabs.addTab(I18n.get("preferences.databaseTab"), databasePanel);
        contentPane.add(tabs, BorderLayout.CENTER);

        JPanel botones = new JPanel();
        botones.add(cancelarButton);
        botones.add(guardarButton);
        contentPane.add(botones, BorderLayout.SOUTH);
    }

    private void cargarConfiguracionActual() {
        AppConfig actual = Main.getAppConfig();
        idiomaPanel.setIdiomaSeleccionado(actual.getIdioma());
        databasePanel.cargarConfiguracion(actual.getDatabase());
    }

    private void configurarAcciones() {
        cancelarButton.addActionListener(e -> volverAlPadre());
        guardarButton.addActionListener(e -> guardar());
    }

    private void guardar() {
        String error = databasePanel.validar();
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, I18n.get("app.error"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ConfigDB database = databasePanel.crearConfiguracion();
            AppConfig nueva = new AppConfig(idiomaPanel.getIdiomaSeleccionado(), database);
            ConfiguracionManager manager = new ConfiguracionManager();
            manager.guardarConfiguracionAplicacion(manager.obtenerRutaConfiguracion(), nueva);

            JOptionPane.showMessageDialog(this,
                    I18n.get("preferences.savedRestart"),
                    I18n.get("preferences.title"),
                    JOptionPane.INFORMATION_MESSAGE);
            volverAlPadre();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    I18n.get("preferences.saveError", ex.getMessage()),
                    I18n.get("app.error"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
