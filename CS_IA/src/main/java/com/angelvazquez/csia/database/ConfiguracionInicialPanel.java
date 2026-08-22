package com.angelvazquez.csia.database;

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

/** Formulario para crear la configuración inicial de motores habilitados. */
final class ConfiguracionInicialPanel extends JPanel {

    static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";
    static final String URL_MYSQL = "jdbc:mysql://localhost:3306/";
    static final String DRIVER_SQLITE = "org.sqlite.JDBC";
    static final String NOMBRE_DB_SQLITE = "CSIA";
    static final String PREFIJO_URL_SQLITE = "jdbc:sqlite:data/";

    private final JComboBox<DatabaseType> campoTipo =
            new JComboBox<>(DatabaseType.enabledValues());
    private final JTextField campoDriver = new JTextField(30);
    private final JTextField campoUrl = new JTextField(30);
    private final JTextField campoDB = new JTextField(30);
    private final JTextField campoUsuario = new JTextField(30);
    private final JPasswordField campoPassword = new JPasswordField(30);
    private final JLabel etiquetaDB = new JLabel("Base de datos:");

    ConfiguracionInicialPanel() {
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
            @Override
            public void insertUpdate(DocumentEvent event) {
                actualizarUrlSqlite();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                actualizarUrlSqlite();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                actualizarUrlSqlite();
            }
        });
    }

    private void construirFormulario() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        agregarFila(0, new JLabel("Tipo de base de datos:"), campoTipo, constraints);
        agregarFila(1, new JLabel("Driver JDBC:"), campoDriver, constraints);
        agregarFila(2, new JLabel("URL:"), campoUrl, constraints);
        agregarFila(3, etiquetaDB, campoDB, constraints);
        agregarFila(4, new JLabel("Usuario:"), campoUsuario, constraints);
        agregarFila(5, new JLabel("Contraseña:"), campoPassword, constraints);
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

        campoDriver.setText(sqlite ? DRIVER_SQLITE : DRIVER_MYSQL);
        campoUrl.setEditable(!sqlite);
        campoUsuario.setEnabled(!sqlite);
        campoPassword.setEnabled(!sqlite);

        if (sqlite) {
            etiquetaDB.setText("Nombre del fichero SQLite:");
            campoDB.setText(NOMBRE_DB_SQLITE);
            campoUsuario.setText("");
            campoPassword.setText("");
            actualizarUrlSqlite();
        } else {
            etiquetaDB.setText("Base de datos:");
            campoDB.setText("");
            campoUrl.setText(URL_MYSQL);
        }
    }

    private void actualizarUrlSqlite() {
        if (obtenerTipo() != DatabaseType.SQLITE) {
            return;
        }
        String nombre = campoDB.getText().trim();
        campoUrl.setText(nombre.isEmpty()
                ? PREFIJO_URL_SQLITE
                : PREFIJO_URL_SQLITE + agregarExtensionDb(nombre));
    }

    private String agregarExtensionDb(String nombre) {
        return nombre.toLowerCase(Locale.ROOT).endsWith(".db")
                ? nombre
                : nombre + ".db";
    }

    String validar() {
        DatabaseType tipo = obtenerTipo();
        if (tipo == null || !tipo.isEnabled()) {
            return "No hay un motor de base de datos habilitado para esta versión.";
        }

        if (campoDriver.getText().isBlank() || campoUrl.getText().isBlank()) {
            return "Driver y URL son obligatorios.";
        }

        if (campoDB.getText().isBlank()) {
            return tipo == DatabaseType.SQLITE
                    ? "Para SQLite, el nombre de la base de datos es obligatorio."
                    : "Para MySQL, la base de datos es obligatoria.";
        }

        if (tipo == DatabaseType.MYSQL
                && campoUsuario.getText().isBlank()) {
            return "Para MySQL, el usuario es obligatorio.";
        }

        if (tipo == DatabaseType.SQLITE) {
            String nombre = campoDB.getText().trim();
            if (nombre.equals(".") || nombre.equals("..")
                    || nombre.matches(".*[\\\\/:*?\"<>|].*")) {
                return "El nombre de SQLite debe ser un nombre de fichero válido, sin rutas.";
            }
        }

        return null;
    }

    ConfigDB crearConfiguracion() {
        ConfigDB configuracion = new ConfigDB();
        configuracion.databaseType = obtenerTipo();
        configuracion.driver = campoDriver.getText().trim();
        configuracion.url = campoUrl.getText().trim();
        configuracion.db = campoDB.getText().trim();

        if (configuracion.databaseType == DatabaseType.SQLITE) {
            configuracion.user = "";
            configuracion.password = "";
        } else {
            configuracion.user = campoUsuario.getText().trim();
            configuracion.password = new String(campoPassword.getPassword());
        }
        return configuracion;
    }

    private DatabaseType obtenerTipo() {
        return (DatabaseType) campoTipo.getSelectedItem();
    }

    void seleccionarTipo(DatabaseType tipo) {
        campoTipo.setSelectedItem(tipo);
    }

    void establecerNombreSqlite(String nombre) {
        campoDB.setText(nombre);
    }

    String obtenerUrl() {
        return campoUrl.getText();
    }

    String obtenerDriver() {
        return campoDriver.getText();
    }

    boolean estanHabilitadasLasCredenciales() {
        return campoUsuario.isEnabled() && campoPassword.isEnabled();
    }

    int numeroTiposDisponibles() {
        return campoTipo.getItemCount();
    }

    boolean contieneTipo(DatabaseType tipo) {
        for (int i = 0; i < campoTipo.getItemCount(); i++) {
            if (campoTipo.getItemAt(i) == tipo) {
                return true;
            }
        }
        return false;
    }
}
