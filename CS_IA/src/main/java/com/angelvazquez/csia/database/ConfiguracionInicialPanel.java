package com.angelvazquez.csia.database;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Formulario de la configuración inicial de la base de datos.
 *
 * La selección del motor determina los valores JDBC predeterminados y los
 * campos que intervienen en la validación.
 */
final class ConfiguracionInicialPanel extends JPanel {

    static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";
    static final String URL_MYSQL = "jdbc:mysql://localhost:3306/";
    static final String DRIVER_SQLITE = "org.sqlite.JDBC";
    static final String URL_SQLITE = "jdbc:sqlite:data/CSIA.db";
    static final String NOMBRE_DB_SQLITE = "CSIA";
    private static final String PREFIJO_URL_SQLITE = "jdbc:sqlite:data/";

    private final JComboBox<DatabaseType> campoTipo =
            new JComboBox<>(DatabaseType.values());

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

    private void configurarSelectorTipo() {
        campoTipo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        cellHasFocus
                );

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

    private void construirFormulario() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        agregarFila(0, "Tipo de base de datos:", campoTipo, constraints);
        agregarFila(1, "Driver JDBC:", campoDriver, constraints);
        agregarFila(2, "URL:", campoUrl, constraints);
        agregarFila(3, etiquetaDB, campoDB, constraints);
        agregarFila(4, "Usuario:", campoUsuario, constraints);
        agregarFila(5, "Contraseña:", campoPassword, constraints);
    }

    private void agregarFila(
            int fila,
            String etiqueta,
            java.awt.Component campo,
            GridBagConstraints constraints) {

        agregarFila(fila, new JLabel(etiqueta), campo, constraints);
    }

    private void agregarFila(
            int fila,
            JLabel etiqueta,
            java.awt.Component campo,
            GridBagConstraints constraints) {

        constraints.gridy = fila;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(etiqueta, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        add(campo, constraints);
    }

    private void aplicarTipoSeleccionado() {
        boolean esSqlite = obtenerTipo() == DatabaseType.SQLITE;

        campoDriver.setText(esSqlite ? DRIVER_SQLITE : DRIVER_MYSQL);
        campoUrl.setEditable(!esSqlite);
        campoDB.setEnabled(true);
        campoUsuario.setEnabled(!esSqlite);
        campoPassword.setEnabled(!esSqlite);

        if (esSqlite) {
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
        return nombre.toLowerCase(java.util.Locale.ROOT).endsWith(".db")
                ? nombre
                : nombre + ".db";
    }

    String validar() {
        if (campoDriver.getText().isBlank()
                || campoUrl.getText().isBlank()) {

            return "Driver y URL son obligatorios.";
        }

        if (obtenerTipo() == DatabaseType.MYSQL
                && (campoDB.getText().isBlank()
                || campoUsuario.getText().isBlank())) {

            return "Para MySQL, la base de datos y el usuario "
                    + "son obligatorios.";
        }

        if (obtenerTipo() == DatabaseType.SQLITE) {
            String nombre = campoDB.getText().trim();

            if (nombre.isEmpty()) {
                return "Para SQLite, el nombre de la base de datos "
                        + "es obligatorio.";
            }

            if (nombre.equals(".")
                    || nombre.equals("..")
                    || nombre.matches(".*[\\\\/:*?\"<>|].*")) {

                return "El nombre de SQLite debe ser un nombre de fichero "
                        + "válido, sin rutas.";
            }
        }

        return null;
    }

    ConfigDB crearConfiguracion() {
        ConfigDB configuracion = new ConfigDB();

        configuracion.databaseType = obtenerTipo();
        configuracion.driver = campoDriver.getText().trim();
        configuracion.url = campoUrl.getText().trim();

        if (configuracion.databaseType == DatabaseType.SQLITE) {
            configuracion.db = campoDB.getText().trim();
            configuracion.user = "";
            configuracion.password = "";
        } else {
            configuracion.db = campoDB.getText().trim();
            configuracion.user = campoUsuario.getText().trim();
            configuracion.password =
                    new String(campoPassword.getPassword());
        }

        return configuracion;
    }

    private DatabaseType obtenerTipo() {
        return (DatabaseType) campoTipo.getSelectedItem();
    }

    void seleccionarTipo(DatabaseType tipo) {
        campoTipo.setSelectedItem(tipo);
    }

    void establecerDatosMysql(
            String baseDatos,
            String usuario,
            String password) {

        campoDB.setText(baseDatos);
        campoUsuario.setText(usuario);
        campoPassword.setText(password);
    }

    void establecerNombreSqlite(String nombre) {
        campoDB.setText(nombre);
    }

    String obtenerNombreBaseDatos() {
        return campoDB.getText();
    }

    String obtenerDriver() {
        return campoDriver.getText();
    }

    String obtenerUrl() {
        return campoUrl.getText();
    }

    boolean esUrlEditable() {
        return campoUrl.isEditable();
    }

    boolean estanHabilitadasLasCredenciales() {
        return campoUsuario.isEnabled() && campoPassword.isEnabled();
    }
}
