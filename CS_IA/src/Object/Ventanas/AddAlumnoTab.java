package Object.Ventanas;

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

import Object.Main;

public class AddAlumnoTab extends JFrame implements ActionListener{
	

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
	JButton AddButton = new JButton("Añadir Alumno");
	JButton AtrasButton = new JButton("Atras");
	
	JLabel lblNewLabel = new JLabel("Introduzca los datos");
	JLabel nombreLabel = new JLabel("Nombre");
	JLabel apellidoLabel = new JLabel("Apellido");
	JLabel DNILabel = new JLabel("DNI");
	
	JTextField nombreField = new JTextField();
	JTextField apellidoField = new JTextField();
	JTextField DNIField = new JTextField();
	static String[] charArray = new String[] {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
	static String[] numArray = new String[] {"1","2","3","4","5","6","7","8","9","0"};
	
	public AddAlumnoTab() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		
		AddButton.setBounds(6, 200, 124, 29);
		AddButton.setFocusable(false);
		AddButton.addActionListener(this);
		contentPane.add(AddButton);
		
		
		lblNewLabel.setBounds(6, 6, 150, 16);
		contentPane.add(lblNewLabel);
		
		
		nombreField.setBounds(6, 100, 100, 26);
		contentPane.add(nombreField);
		nombreField.setColumns(10);
		
		
		nombreLabel.setBounds(6, 75, 61, 16);
		contentPane.add(nombreLabel);
		
		
		apellidoLabel.setBounds(125, 75, 61, 16);
		contentPane.add(apellidoLabel);
		
		
		apellidoField.setColumns(10);
		apellidoField.setBounds(125, 100, 100, 26);
		contentPane.add(apellidoField);
		
		
		DNILabel.setBounds(250, 75, 61, 16);
		contentPane.add(DNILabel);
		
		
		DNIField.setColumns(10);
		DNIField.setBounds(250, 100, 100, 26);
		contentPane.add(DNIField);
		
		
		AtrasButton.setBounds(150, 200, 117, 29);
		AtrasButton.setFocusable(false);
		AtrasButton.addActionListener(this);
		contentPane.add(AtrasButton);

	}

	static public String CrearID() {
		
		String ID = "";
		
		for(int i=0; i<8;i++) {
			ID = ID + Main.randomChar(charArray);
		}
		for(int i=0; i<4;i++) {
			ID = ID + Main.randomChar(numArray);
		}
		/*
		if(IDQueExiste == ID) {
			ID = CrearID();
		}*/
		return ID;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		
	}

}
