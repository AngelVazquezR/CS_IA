package com.angelvazquez.csia.ui.ventanas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.angelvazquez.csia.Main;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.repository.UsuarioRepository;
import com.angelvazquez.csia.security.AuthService;
import com.angelvazquez.csia.security.PasswordHasher;

public class LoginPage implements ActionListener {

    private final JFrame frame = new JFrame();
    private final JButton loginbutton = new JButton("Login");
    private final JTextField userIDField = new JTextField("");
    private final JPasswordField userPasswordField = new JPasswordField("");
    private final JLabel userIDLabel = new JLabel("Usuario:");
    private final JLabel userPasswordLabel = new JLabel("Contraseña:");
    private final AuthService authService;

    public LoginPage() {
        UsuarioRepository repository = new UsuarioRepository(
                new DatabaseConnectionFactory(), Main.getConfiguracion());
        authService = new AuthService(repository, new PasswordHasher());

        userIDLabel.setBounds(50, 100, 75, 25);
        userPasswordLabel.setBounds(35, 150, 90, 25);
        userIDField.setBounds(125, 100, 200, 25);
        userPasswordField.setBounds(125, 150, 200, 25);
        loginbutton.setBounds(125, 200, 100, 25);
        loginbutton.setFocusable(false);
        loginbutton.addActionListener(this);

        frame.add(userIDLabel);
        frame.add(userPasswordLabel);
        frame.add(userIDField);
        frame.add(userPasswordField);
        frame.add(loginbutton);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 420);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() != loginbutton) {
            return;
        }

        try {
            boolean correcto = authService.autenticar(
                    userIDField.getText(), userPasswordField.getPassword());

            if (correcto) {
                WelcomePage welcomepage = new WelcomePage();
                welcomepage.setVisible(true);
                frame.dispose();
            } else {
                mostrarCredencialesIncorrectas();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    frame,
                    "No se ha podido consultar el usuario.\n" + ex.getMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            userPasswordField.setText("");
        }
    }

    private void mostrarCredencialesIncorrectas() {
        JOptionPane.showMessageDialog(
                frame,
                "Usuario o contraseña incorrecta",
                "Acceso denegado",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
