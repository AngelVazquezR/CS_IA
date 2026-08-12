package com.angelvazquez.csia.ui.ventanas;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.ElementType;

import com.angelvazquez.csia.database.ConectionSQL;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JRadioButton;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.JButton;
import javax.swing.ButtonGroup;

public class GestionarTab extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	JTextField ApellidoField = new JTextField("");
	JTextField NombreField = new JTextField("");
	JTextField DNIField = new JTextField();
	
	JButton AtrasButton = new JButton("Atras");
	JButton ActualizarButton = new JButton("Actualizar");
	
	JRadioButton AddRadial = new JRadioButton("Añadir");
	JRadioButton ModificarRadial = new JRadioButton("Modificar");
	JRadioButton EliminarRadial = new JRadioButton("Eliminar");
	JRadioButton ProfesorRadial = new JRadioButton("Profesor");
	JRadioButton AlumnoRadial = new JRadioButton("Alumno");
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	

	public int accion = 0;
	public int profe = 0;
	
	public GestionarTab() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Nombre");
		lblNewLabel.setBounds(6, 6, 61, 16);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Apellido");
		lblNewLabel_1.setBounds(200, 6, 61, 16);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("DNI");
		lblNewLabel_2.setBounds(400, 6, 61, 16);
		contentPane.add(lblNewLabel_2);
		
		
		ApellidoField.setBounds(200, 34, 130, 26);
		contentPane.add(ApellidoField);
		ApellidoField.setColumns(10);
		
		
		NombreField.setBounds(6, 34, 130, 26);
		contentPane.add(NombreField);
		NombreField.setColumns(10);
		
		
		DNIField.setBounds(400, 34, 130, 26);
		contentPane.add(DNIField);
		DNIField.setColumns(10);
		buttonGroup.add(AddRadial);
		
		
		AddRadial.setBounds(6, 90, 141, 23);
		AddRadial.addActionListener(this);
		contentPane.add(AddRadial);
		
		
		buttonGroup.add(ModificarRadial);				
		ModificarRadial.setBounds(6, 125, 141, 23);
		ModificarRadial.addActionListener(this);
		contentPane.add(ModificarRadial);
		
		buttonGroup.add(EliminarRadial);
		EliminarRadial.setBounds(6, 160, 141, 23);
		EliminarRadial.addActionListener(this);
		contentPane.add(EliminarRadial);
		buttonGroup_1.add(ProfesorRadial);
		
		
		ProfesorRadial.setBounds(189, 90, 141, 23);
		ProfesorRadial.addActionListener(this);
		contentPane.add(ProfesorRadial);
		buttonGroup_1.add(AlumnoRadial);
		
		
		AlumnoRadial.setBounds(189, 125, 141, 23);
		AlumnoRadial.addActionListener(this);
		contentPane.add(AlumnoRadial);
		
		DatePicker datePicker = new DatePicker();
		datePicker.setBounds(374, 100, 220, 29);
		contentPane.add(datePicker);
		
		
		ActualizarButton.setBounds(6, 217, 117, 29);
		ActualizarButton.addActionListener(this);
		contentPane.add(ActualizarButton);
		
		JLabel lblNewLabel_3 = new JLabel("Accion");
		lblNewLabel_3.setBounds(16, 72, 61, 16);
		contentPane.add(lblNewLabel_3);
		
		JLabel lblNewLabel_4 = new JLabel("Tipo de persona");
		lblNewLabel_4.setBounds(200, 72, 106, 16);
		contentPane.add(lblNewLabel_4);
		
		
		AtrasButton.setBounds(6, 258, 117, 29);
		AtrasButton.addActionListener(this);
		contentPane.add(AtrasButton);
		
		JLabel lblNewLabel_5 = new JLabel("Fecha de alta");
		lblNewLabel_5.setBounds(374, 72, 94, 16);
		contentPane.add(lblNewLabel_5);

	}
	
	
	
	public void CerrarVentana() {
		Component com = SwingUtilities.getRoot(this);
		 ((Window) com).dispose();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==AtrasButton) {
			WelcomePage.RestaurarVentana();
			//Main.Welcome();
			CerrarVentana();
		}
		if(e.getSource()==ActualizarButton) {
			String Nombre = NombreField.getText();
			String Apellido = ApellidoField.getText();
			String DNI = DNIField.getText();
			if(accion != 0 && profe != 0 && Apellido != null && Nombre != null && ConectionSQL.existeDNI(profe, DNI) == false) {
				System.out.println("Todos los datos estan en orden");
				if(profe == 1) {
					if (accion == 1) {
						ConectionSQL.ModProfe(Nombre, Apellido, DNI, "",  "");
					}else if(accion == 2) {
						ConectionSQL.AddProfe(Nombre, Apellido, DNI, "",  "");
						NombreField.setText("");
						ApellidoField.setText("");
						DNIField.setText("");
					}else {
						ConectionSQL.DeleteProfe(Nombre, Apellido, DNI);
					}
				}else{
					if (accion == 1) {
						ConectionSQL.ModAlumno(Nombre, Apellido, DNI);
					}else if(accion == 2) {
						ConectionSQL.AddAlumno(Nombre, Apellido, DNI);
						NombreField.setText("");
						ApellidoField.setText("");
						DNIField.setText("");
					}else {
						ConectionSQL.DeleteAlumno(Nombre, Apellido, DNI);
					}
				}
			}else {
				System.out.println("Rellena los campos que faltan");
				NombreField.setText("");
				ApellidoField.setText("");
				DNIField.setText("");
			}
		}
		if(e.getSource()==ModificarRadial) {
			accion= 1;
		}
		if(e.getSource()==AddRadial) {
			accion= 2;
		}
		if(e.getSource()==EliminarRadial) {
			accion= 3;
		}
		if(e.getSource()==ProfesorRadial) {
			profe = 1;
		}
		if(e.getSource()==AlumnoRadial) {
			profe = 2;
		}
	}
}
