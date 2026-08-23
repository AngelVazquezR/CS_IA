package com.angelvazquez.csia.ui.ventanas;

import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.repository.UsuarioRepository;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.security.AuthService;
import com.angelvazquez.csia.security.PasswordHasher;

/** Flujo exclusivo para crear el primer usuario de una instalación nueva. */
public final class RegistroInicialUsuario {

    private RegistroInicialUsuario() {
    }

    public static boolean solicitar(ConfigDB configuracion) {
        UsuarioRepository repository = new UsuarioRepository(
                new DatabaseConnectionFactory(), configuracion);
        AuthService authService = new AuthService(repository, new PasswordHasher());

        JTextField usuarioField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        JPanel panel = new JPanel();
        panel.add(new JLabel(I18n.get("login.user")));
        panel.add(usuarioField);
        panel.add(new JLabel(I18n.get("login.password")));
        panel.add(passwordField);

        while (true) {
            int resultado = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    I18n.get("user.initial.title"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (resultado != JOptionPane.OK_OPTION) {
                passwordField.setText("");
                return false;
            }

            try {
                authService.registrar(usuarioField.getText(), passwordField.getPassword());
                JOptionPane.showMessageDialog(
                        null,
                        I18n.get("user.initial.success"),
                        I18n.get("user.initial.configTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(
                        null,
                        e.getMessage(),
                        I18n.get("user.register.invalidTitle"),
                        JOptionPane.WARNING_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        null,
                        I18n.get("user.initial.createError", e.getMessage()),
                        I18n.get("database.error.title"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            } finally {
                passwordField.setText("");
            }
        }
    }
}
