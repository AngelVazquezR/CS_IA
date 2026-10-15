package com.angelvazquez.csia.ui.ventanas;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseType;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.angelvazquez.csia.i18n.I18n;

/** Formulario reutilizable para configurar motores de base de datos habilitados. */
public final class ConfiguracionInicialPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
    private final JComboBox<DatabaseType> campoTipo =
            new JComboBox<>(DatabaseType.enabledValues());
    private final JTextField campoDriver = new JTextField(30);
    private final JTextField campoUrl = new JTextField(30);
    private final JTextField campoDB = new JTextField(30);
    private final JTextField campoUsuario = new JTextField(30);
    private final JPasswordField campoPassword = new JPasswordField(30);
    private final JLabel etiquetaDB = new JLabel();

    public ConfiguracionInicialPanel() {
        super(new GridBagLayout());
        configurarSelectorTipo();
        configurarNombreSqlite();
        construirFormulario();
        aplicarTipoSeleccionado();
    }

    private void configurarSelectorTipo() {
        campoTipo.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value == DatabaseType.MYSQL) {
                    setText("MySQL");
                } else if (value == DatabaseType.SQLITE) {
                    setText("SQLite");
                }
                return this;
            }
        });
        campoTipo.addActionListener(event -> aplicarTipoSeleccionado());
    }

    private void configurarNombreSqlite() {
        campoDB.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { actualizarUrlSqlite(); }
            @Override public void removeUpdate(DocumentEvent event) { actualizarUrlSqlite(); }
            @Override public void changedUpdate(DocumentEvent event) { actualizarUrlSqlite(); }
        });
    }

    private void construirFormulario() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        agregarFila(0, new JLabel(I18n.get("database.type.label")), campoTipo, constraints);
        agregarFila(1, new JLabel(I18n.get("database.driver.label")), campoDriver, constraints);
        agregarFila(2, new JLabel(I18n.get("database.url.label")), campoUrl, constraints);
        agregarFila(3, etiquetaDB, campoDB, constraints);
        agregarFila(4, new JLabel(I18n.get("database.user.label")), campoUsuario, constraints);
        agregarFila(5, new JLabel(I18n.get("database.password.label")), campoPassword, constraints);
    }

    private void agregarFila(int fila, JLabel etiqueta,
            java.awt.Component campo, GridBagConstraints constraints) {
        constraints.gridy = fila;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(etiqueta, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        add(campo, constraints);
    }

    private void aplicarTipoSeleccionado() {
        boolean sqlite = obtenerTipo() == DatabaseType.SQLITE;
        campoDriver.setText(sqlite ? ConfigDB.DRIVER_SQLITE : ConfigDB.DRIVER_MYSQL);
        campoUrl.setEditable(!sqlite);
        campoUsuario.setEnabled(!sqlite);
        campoPassword.setEnabled(!sqlite);

        if (sqlite) {
            etiquetaDB.setText(I18n.get("database.sqlite.file.label"));
            campoDB.setText(ConfigDB.NOMBRE_DB_SQLITE);
            campoUsuario.setText("");
            campoPassword.setText("");
            actualizarUrlSqlite();
        } else {
            etiquetaDB.setText(I18n.get("database.name.label"));
            campoDB.setText("");
            campoUrl.setText(ConfigDB.URL_MYSQL);
        }
    }

    public void cargarConfiguracion(ConfigDB configuracion) {
        if (configuracion == null) return;
        campoTipo.setSelectedItem(configuracion.getDatabaseType());
        campoDriver.setText(valor(configuracion.getDriver()));
        campoUrl.setText(valor(configuracion.getUrl()));
        campoDB.setText(valor(configuracion.getDb()));
        campoUsuario.setText(valor(configuracion.getUser()));
        campoPassword.setText(valor(configuracion.getPassword()));
        if (configuracion.getDatabaseType() == DatabaseType.SQLITE) actualizarUrlSqlite();
    }

    private String valor(String valor) { return valor == null ? "" : valor; }

    private void actualizarUrlSqlite() {
        if (obtenerTipo() != DatabaseType.SQLITE) return;
        String nombre = campoDB.getText().trim();
        campoUrl.setText(nombre.isEmpty() ? ConfigDB.PREFIJO_URL_SQLITE
                : ConfigDB.PREFIJO_URL_SQLITE + agregarExtensionDb(nombre));
    }

    private String agregarExtensionDb(String nombre) {
        return nombre.toLowerCase(Locale.ROOT).endsWith(".db") ? nombre : nombre + ".db";
    }

    public String validar() {
        DatabaseType tipo = obtenerTipo();
        if (tipo == null || !tipo.isEnabled()) return I18n.get("database.validation.noEngine");
        if (campoDriver.getText().isBlank() || campoUrl.getText().isBlank())
            return I18n.get("database.validation.driverUrlRequired");
        if (campoDB.getText().isBlank()) return tipo == DatabaseType.SQLITE
                ? I18n.get("database.validation.sqliteNameRequired")
                : I18n.get("database.validation.mysqlNameRequired");
        if (tipo == DatabaseType.MYSQL && campoUsuario.getText().isBlank())
            return I18n.get("database.validation.mysqlUserRequired");
        if (tipo == DatabaseType.SQLITE) {
            String nombre = campoDB.getText().trim();
            if (nombre.equals(".") || nombre.equals("..")
                    || nombre.matches(".*[\\\\/:*?\"<>|].*"))
                return I18n.get("database.validation.sqliteInvalidName");
        }
        return null;
    }

    public ConfigDB crearConfiguracion() {
        ConfigDB configuracion = new ConfigDB();
        configuracion.setDatabaseType(obtenerTipo());
        configuracion.setDriver(campoDriver.getText().trim());
        configuracion.setUrl(campoUrl.getText().trim());
        configuracion.setDb(campoDB.getText().trim());
        if (configuracion.getDatabaseType() == DatabaseType.SQLITE) {
            configuracion.setUser("");
            configuracion.setPassword("");
        } else {
            configuracion.setUser(campoUsuario.getText().trim());
            configuracion.setPassword(new String(campoPassword.getPassword()));
        }
        return configuracion;
    }

    private DatabaseType obtenerTipo() { return (DatabaseType) campoTipo.getSelectedItem(); }
    void seleccionarTipo(DatabaseType tipo) { campoTipo.setSelectedItem(tipo); }
    void establecerNombreSqlite(String nombre) { campoDB.setText(nombre); }
    String obtenerUrl() { return campoUrl.getText(); }
    String obtenerDriver() { return campoDriver.getText(); }
    boolean estanHabilitadasLasCredenciales() { return campoUsuario.isEnabled() && campoPassword.isEnabled(); }
    int numeroTiposDisponibles() { return campoTipo.getItemCount(); }
    boolean contieneTipo(DatabaseType tipo) {
        for (int i = 0; i < campoTipo.getItemCount(); i++) if (campoTipo.getItemAt(i) == tipo) return true;
        return false;
    }
}
