package com.angelvazquez.csia.ui.ventanas;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.angelvazquez.csia.Main;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.repository.UsuarioRepository;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.security.AuthService;
import com.angelvazquez.csia.security.PasswordHasher;

public class RegistarTab extends VentanaSecundaria implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final JPanel contentPane;
    private final JTextField usuarioField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton registrarButton = new JButton(I18n.get("user.register.button"));
    private final JButton atrasButton = new JButton(I18n.get("app.back"));
    private final AuthService authService;

    public RegistarTab() { this(null); }

    public RegistarTab(Window parent) {
        super(parent);
        UsuarioRepository repository = new UsuarioRepository(
                new DatabaseConnectionFactory(), Main.getConfiguracion());
        authService = new AuthService(repository, new PasswordHasher());
        setTitle(I18n.get("user.register.title"));
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);
        JLabel titulo = new JLabel(I18n.get("user.register.title"));
        titulo.setBounds(6, 6, 180, 16); contentPane.add(titulo);
        JLabel usuarioLabel = new JLabel(I18n.get("user.register.user"));
        usuarioLabel.setBounds(25, 72, 100, 16); contentPane.add(usuarioLabel);
        usuarioField.setBounds(25, 100, 130, 26); contentPane.add(usuarioField);
        JLabel passwordLabel = new JLabel(I18n.get("user.register.password"));
        passwordLabel.setBounds(200, 72, 90, 16); contentPane.add(passwordLabel);
        passwordField.setBounds(200, 100, 130, 26); contentPane.add(passwordField);
        registrarButton.setBounds(25, 150, 151, 29);
        registrarButton.addActionListener(this); contentPane.add(registrarButton);
        atrasButton.setBounds(6, 237, 80, 29);
        atrasButton.addActionListener(this); contentPane.add(atrasButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registrarButton) registrarUsuario();
        else if (e.getSource() == atrasButton) volverAlPadre();
    }

    private void registrarUsuario() {
        try {
            authService.registrar(usuarioField.getText(), passwordField.getPassword());
            JOptionPane.showMessageDialog(this, I18n.get("user.register.success"),
                    I18n.get("user.register.dialogTitle"), JOptionPane.INFORMATION_MESSAGE);
            usuarioField.setText("");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    I18n.get("user.register.invalidTitle"), JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    I18n.get("user.register.error", ex.getMessage()),
                    I18n.get("database.error.title"), JOptionPane.ERROR_MESSAGE);
        } finally {
            passwordField.setText("");
        }
    }
}
