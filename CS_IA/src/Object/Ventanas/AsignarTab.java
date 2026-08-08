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
import javax.swing.JRadioButton;
import com.github.lgooddatepicker.components.TimePicker;

import Object.ConectionSQL;
import Object.Main;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JFormattedTextField;
import javax.swing.JComboBox;

public class AsignarTab extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	JLabel AsignarLabel = new JLabel("Asiganar");
	JLabel ProfeLabel = new JLabel("Nombre del profesor");
	JLabel AlumnoLabel = new JLabel("Nombre del alumno");
	
	JButton AsignarButton = new JButton("Asignar");
	JButton AtrasButton = new JButton("Atras");
	
	
	
	static JComboBox<String> profeComboBox = new JComboBox<>();
	static JComboBox<String> alumComboBox = new JComboBox<>();
	
	
	public AsignarTab() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 500, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		AsignarLabel.setBounds(6, 6, 61, 16);
		contentPane.add(AsignarLabel);
		
		ProfeLabel.setBounds(16, 37, 130, 16);
		contentPane.add(ProfeLabel);
		
		AlumnoLabel.setBounds(170, 37, 142, 16);
		contentPane.add(AlumnoLabel);
		

		
		AsignarButton.setBounds(101, 287, 117, 29);
		AsignarButton.addActionListener(this);
		contentPane.add(AsignarButton);
		
		
		AtrasButton.setBounds(6, 287, 83, 29);
		AtrasButton.addActionListener(this);
		contentPane.add(AtrasButton);
		
		JLabel lblNewLabel = new JLabel("Dias");
		lblNewLabel.setBounds(16, 100, 61, 16);
		contentPane.add(lblNewLabel);
		
		JRadioButton rdbtnNewRadioButton = new JRadioButton("Lunes");
		rdbtnNewRadioButton.setBounds(6, 122, 83, 23);
		contentPane.add(rdbtnNewRadioButton);
		
		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("Martes");
		rdbtnNewRadioButton_1.setBounds(5, 145, 84, 23);
		contentPane.add(rdbtnNewRadioButton_1);
		
		JRadioButton rdbtnNewRadioButton_2 = new JRadioButton("Miercoles");
		rdbtnNewRadioButton_2.setBounds(6, 169, 97, 23);
		contentPane.add(rdbtnNewRadioButton_2);
		
		JRadioButton rdbtnNewRadioButton_3 = new JRadioButton("Jueves");
		rdbtnNewRadioButton_3.setBounds(101, 122, 80, 23);
		contentPane.add(rdbtnNewRadioButton_3);
		
		JRadioButton rdbtnNewRadioButton_4 = new JRadioButton("Viernes");
		rdbtnNewRadioButton_4.setBounds(101, 145, 80, 23);
		contentPane.add(rdbtnNewRadioButton_4);
		
		JRadioButton rdbtnNewRadioButton_5 = new JRadioButton("Sabado");
		rdbtnNewRadioButton_5.setBounds(101, 169, 83, 23);
		contentPane.add(rdbtnNewRadioButton_5);
		
		JRadioButton rdbtnNewRadioButton_6 = new JRadioButton("Domingo");
		rdbtnNewRadioButton_6.setBounds(193, 122, 97, 23);
		contentPane.add(rdbtnNewRadioButton_6);
		
		JLabel lblNewLabel_1 = new JLabel("Hora de inicio");
		lblNewLabel_1.setBounds(6, 202, 97, 16);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Hora de final");
		lblNewLabel_2.setBounds(159, 202, 83, 16);
		contentPane.add(lblNewLabel_2);
		
		TimePicker timePicker = new TimePicker();
		timePicker.setBounds(6, 230, 145, 29);
		contentPane.add(timePicker);
		
		TimePicker timePicker_1 = new TimePicker();
		timePicker_1.setBounds(158, 230, 145, 29);
		contentPane.add(timePicker_1);
		
		alumComboBox.addItem("Alumno");
		alumComboBox.setBounds(180, 65, 107, 27);
		contentPane.add(alumComboBox);
		
		profeComboBox.addItem("Profesor");
		profeComboBox.setBounds(26, 65, 107, 27);
		contentPane.add(profeComboBox);
		

	     // Crear elementos de menú
	     
	     ConectionSQL.FillAlumPop();
	     ConectionSQL.FillProfePop();

	}

	public void CerrarVentana() {
		Component com = SwingUtilities.getRoot(this);
		 ((Window) com).dispose();
	}

	public static void AddAlumPop(String alum) {	
		alumComboBox.addItem(alum);			
	}
	public static void AddProfePop(String alum) {	
		profeComboBox.addItem(alum);			
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==AtrasButton) {
			WelcomePage.RestaurarVentana();
			//Main.Welcome();
			CerrarVentana();
		}
		if (e.getSource()== AsignarButton) {
			if (!"Profesor".equals(profeComboBox.getSelectedItem())
					&& !"Alumno".equals(alumComboBox.getSelectedItem())) {
				
			
			ConectionSQL.AsignarProf( profeComboBox.getSelectedItem().toString().toUpperCase(), 
					alumComboBox.getSelectedItem().toString().toUpperCase());
			}
		}
		
	}
	
	
}
