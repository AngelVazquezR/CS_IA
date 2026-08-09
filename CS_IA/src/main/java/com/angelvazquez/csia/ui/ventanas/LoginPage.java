package com.angelvazquez.csia.ui.ventanas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.angelvazquez.csia.database.ConectionSQL;

import javax.swing.JOptionPane;


public class LoginPage implements ActionListener {
	
	JFrame frame = new JFrame();
	JButton loginbutton = new JButton("Login");
	JTextField userIDField = new JTextField("");
	JPasswordField userPasswordField = new JPasswordField("");
	JLabel userIDLabel = new JLabel("User ID:");
	JLabel userPasswordLabel = new JLabel("User password:");
	HashMap<String,String> logininfo = new HashMap<String,String>();
	
	public LoginPage(HashMap<String,String> loginInfoOriginal){
		logininfo = loginInfoOriginal;
		
		userIDLabel.setBounds(50,100,75,25);
		userPasswordLabel.setBounds(0,150,150,25);
		
		userIDField.setBounds(125,100,200,25);
		userPasswordField.setBounds(125,150,200,25);
		
		loginbutton.setBounds(125,200,100,25);
		loginbutton.setFocusable(false);
		loginbutton.addActionListener(this);
				
		
		frame.add(userIDLabel);
		frame.add(userPasswordLabel);
		frame.add(userIDField);
		frame.add(userPasswordField);
		frame.add(loginbutton);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(420,420);
		frame.setLayout(null);
		frame.setVisible(true);
		
		
		
		
	}

	public String getHash(String password) {
		return password;
	}
	
	public String TransformPassword(String password) {
		String hashPassword;
		
		
		
		hashPassword = password;
		return hashPassword;
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource()==loginbutton) {
			
			String userID = userIDField.getText();
			String password = String.valueOf(userPasswordField.getPassword());
			String hashPassword ="";
			
			hashPassword = ConectionSQL.RecuperaPassword(userID);
			try {
				ConectionSQL.Conection();
			} catch (SQLException e1) {
				
				e1.printStackTrace();
			}
			
			if(hashPassword.equals(password)) {
				System.out.println("Correct Login");

				WelcomePage welcomepage = new WelcomePage();
				welcomepage.setVisible(true);
				frame.dispose();
			}else {
				JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrecta", null,JOptionPane.INFORMATION_MESSAGE);
			}
			/*
			if(logininfo.containsKey(userID)){
				if (logininfo.get(userID).equals(password)) {
					System.out.println("Correct Login");
					
					WelcomePage welcomepage = new WelcomePage();
					welcomepage.setVisible(true);
					frame.dispose();
					
				}
			}*/
			
		}
		
	}

}
