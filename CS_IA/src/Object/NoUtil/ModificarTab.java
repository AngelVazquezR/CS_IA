package Object.NoUtil;

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
import javax.swing.JRadioButton;
import com.github.lgooddatepicker.components.DatePicker;



public class ModificarTab extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JTextField NombreField;
	private JTextField ApellidoField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;

	

	
	public ModificarTab() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 525, 375);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(6, 50, 101, 26);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("ID actual");
		lblNewLabel.setBounds(6, 32, 61, 16);
		contentPane.add(lblNewLabel);
		
		JRadioButton TeacherRad = new JRadioButton("Tutor");
		TeacherRad.setBounds(6, 88, 86, 23);
		contentPane.add(TeacherRad);
		
		JRadioButton AlumnoRad = new JRadioButton("Alumno");
		AlumnoRad.setBounds(6, 123, 86, 23);
		contentPane.add(AlumnoRad);
		
		JLabel lblNewLabel_1 = new JLabel("Modificar");
		lblNewLabel_1.setBounds(6, 6, 61, 16);
		contentPane.add(lblNewLabel_1);
		
		NombreField = new JTextField();
		NombreField.setBounds(133, 50, 130, 26);
		contentPane.add(NombreField);
		NombreField.setColumns(10);
		
		ApellidoField = new JTextField();
		ApellidoField.setBounds(275, 50, 130, 26);
		contentPane.add(ApellidoField);
		ApellidoField.setColumns(10);
		
		JLabel NApellidoLabel = new JLabel("Nuevo apellido");
		NApellidoLabel.setBounds(275, 32, 101, 16);
		contentPane.add(NApellidoLabel);
		
		JLabel NNombreLabel = new JLabel("Nuevo nombre");
		NNombreLabel.setBounds(133, 32, 101, 16);
		contentPane.add(NNombreLabel);
		
		JLabel BajaLabel = new JLabel("Fecha de alta");
		BajaLabel.setBounds(16, 158, 101, 16);
		contentPane.add(BajaLabel);
		
		JLabel DiaLabel = new JLabel("Dia");
		DiaLabel.setBounds(6, 186, 61, 16);
		contentPane.add(DiaLabel);
		
		JLabel MesLabel = new JLabel("Mes");
		MesLabel.setBounds(67, 186, 61, 16);
		contentPane.add(MesLabel);
		
		JLabel AñoLabel = new JLabel("Año");
		AñoLabel.setBounds(121, 186, 61, 16);
		contentPane.add(AñoLabel);
		
		textField_1 = new JTextField();
		textField_1.setBounds(6, 214, 50, 26);
		contentPane.add(textField_1);
		textField_1.setColumns(10);
		
		textField_2 = new JTextField();
		textField_2.setBounds(67, 214, 40, 26);
		contentPane.add(textField_2);
		textField_2.setColumns(10);
		
		textField_3 = new JTextField();
		textField_3.setBounds(120, 214, 50, 26);
		contentPane.add(textField_3);
		textField_3.setColumns(10);
		
		JButton ModButton = new JButton("Modificar");
		ModButton.setBounds(77, 243, 117, 29);
		contentPane.add(ModButton);
		
		JButton AtrasButton = new JButton("Atras");
		AtrasButton.setBounds(6, 243, 69, 29);
		contentPane.add(AtrasButton);
		
		DatePicker datePicker = new DatePicker();
		datePicker.setBounds(197, 86, 220, 29);
		contentPane.add(datePicker);

	}



	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
