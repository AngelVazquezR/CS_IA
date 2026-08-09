package com.angelvazquez.csia.ui.ventanas;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.angelvazquez.csia.database.ConectionSQL;
import com.angelvazquez.csia.util.Algoritmos;
import com.angelvazquez.csia.Main;

public class RegistarTab extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	JTextField UsuarioField = new JTextField();
	JTextField PasswordField = new JTextField();
	JButton RegistrarButton = new JButton("Registrar usuario");
	private final JButton AtrasButton = new JButton("Atras");
	private final JLabel lblNewLabel = new JLabel("Registrar nuevo usuario");

	
	public RegistarTab() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		
		UsuarioField.setBounds(25, 100, 130, 26);
		contentPane.add(UsuarioField);
		UsuarioField.setColumns(10);
		
		
		PasswordField.setBounds(200, 100, 130, 26);
		contentPane.add(PasswordField);
		PasswordField.setColumns(10);
		
		JLabel NombreLabel = new JLabel("Nuevo usuario");
		NombreLabel.setBounds(25, 72, 100, 16);
		contentPane.add(NombreLabel);
		
		JLabel ApellidoLabel = new JLabel("Contraseña");
		ApellidoLabel.setBounds(200, 72, 76, 16);
		contentPane.add(ApellidoLabel);
		
		
		RegistrarButton.setBounds(25, 150, 151, 29);
		RegistrarButton.addActionListener(this);
		RegistrarButton.setFocusable(false);
		contentPane.add(RegistrarButton);
		AtrasButton.setBounds(6, 237, 70, 29);
		
		contentPane.add(AtrasButton);
		lblNewLabel.setBounds(6, 6, 160, 16);
		AtrasButton.addActionListener(this);
		
		contentPane.add(lblNewLabel);
		

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		ConectionSQL conn=new ConectionSQL();
		
		if(e.getSource()==RegistrarButton) {
			
			String user = UsuarioField.getText();
			String password = PasswordField.getText();
			password = Algoritmos.hashAlgorithm(password);
			System.out.println("Escuchado, los parametros son "+user+" y "+password);
			conn.RegistrarUsuario(user,password);
		}
		if(e.getSource()==AtrasButton) {
			Main.Welcome();
			CerrarVentana();
		}
	}
	
	public void CerrarVentana() {
		Component com = SwingUtilities.getRoot(this);
		 ((Window) com).dispose();
	}
}
