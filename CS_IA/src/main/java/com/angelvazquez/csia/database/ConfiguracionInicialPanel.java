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

    private final JComboBox<DatabaseType> campoTipo =
            new JComboBox<>(DatabaseType.values());

    private final JTextField campoDriver = new JTextField(30);
    private final JTextField campoUrl = new JTextField(30);
    private final JTextField campoDB = new JTextField(30);
    private final JTextField campoUsuario = new JTextField(30);
    private final JPasswordField campoPassword = new JPasswordField(30);

    ConfiguracionInicialPanel() {
        super(new GridBagLayout());

        configurarSelectorTipo();
        construirFormulario();
        aplicarTipoSeleccionado();
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
        agregarFila(3, "Base de datos:", campoDB, constraints);
        agregarFila(4, "Usuario:", campoUsuario, constraints);
        agregarFila(5, "Contraseña:", campoPassword, constraints);
    }

    private void agregarFila(
            int fila,
            String etiqueta,
            java.awt.Component campo,
            GridBagConstraints constraints) {

        constraints.gridy = fila;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(new JLabel(etiqueta), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        add(campo, constraints);
    }

    private void aplicarTipoSeleccionado() {
        boolean esSqlite = obtenerTipo() == DatabaseType.SQLITE;

        campoDriver.setText(esSqlite ? DRIVER_SQLITE : DRIVER_MYSQL);
        campoUrl.setText(esSqlite ? URL_SQLITE : URL_MYSQL);

        campoDB.setEnabled(!esSqlite);
        campoUsuario.setEnabled(!esSqlite);
        campoPassword.setEnabled(!esSqlite);

        if (esSqlite) {
            campoDB.setText("");
            campoUsuario.setText("");
            campoPassword.setText("");
        }
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

        return null;
    }

    ConfigDB crearConfiguracion() {
        ConfigDB configuracion = new ConfigDB();

        configuracion.databaseType = obtenerTipo();
        configuracion.driver = campoDriver.getText().trim();
        configuracion.url = campoUrl.getText().trim();

        if (configuracion.databaseType == DatabaseType.SQLITE) {
            configuracion.db = "";
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

    String obtenerDriver() {
        return campoDriver.getText();
    }

    String obtenerUrl() {
        return campoUrl.getText();
    }

    boolean estanHabilitadasLasCredenciales() {
        return campoUsuario.isEnabled() && campoPassword.isEnabled();
    }
}
