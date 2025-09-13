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
import Object.NoUtil.ModificarTab;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;



public class WelcomePage extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	//private JPanel contentPane;
	
	JPanel contentPane = new JPanel();
	
	JLabel WelcomeLabel = new JLabel("Home");
	JLabel TablaLabel = new JLabel("Ver tabla de:");
	
	JButton Actualizarbtn = new JButton("Actualizar registro");
	JButton TablaProfesorbtn = new JButton("Tutor");
	JButton TablaAlumnobtn = new JButton("Alumno");
	JButton Asignarbtn = new JButton("Asignar profesor");
	JButton Logoutbtn = new JButton("Cerrar sesión");
	JButton NuevoUserbtn = new JButton("Crear usuario");
	private final JMenu mnAcciones = new JMenu("Acciones");
	private final JMenuItem mniAccionesModificar = new JMenuItem("Modificar");
	private final JMenuItem mniAccionesSalir = new JMenuItem("Salir");
	private final JSeparator mniAccionesSeparador1 = new JSeparator();
	private final JMenuItem mniAccionesAsignar = new JMenuItem("Asignar");
	private final JSeparator mniAccionesSeparador2 = new JSeparator();
	
	private final JMenu mnTablas = new JMenu("Tablas");
	JMenuItem mntmProfeTabla = new JMenuItem("Profesores");
	JMenuItem mntmAlumnoTabla = new JMenuItem("Alumnos");

	
	public WelcomePage() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 402, 319);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		menuBar.add(mnAcciones);
		
		mnAcciones.add(mniAccionesModificar);
		mniAccionesModificar.addActionListener(this);
		
		mnAcciones.add(mniAccionesSeparador1);
		
		mnAcciones.add(mniAccionesAsignar);
		mniAccionesAsignar.addActionListener(this);
		
		mnAcciones.add(mniAccionesSeparador2);
		
		mnAcciones.add(mniAccionesSalir);
		
		menuBar.add(mnTablas);
		
		
		mnTablas.add(mntmProfeTabla);
		mntmProfeTabla.addActionListener(this);
		
		JSeparator separator = new JSeparator();
		mnTablas.add(separator);
		
		
		mnTablas.add(mntmAlumnoTabla);
		mntmAlumnoTabla.addActionListener(this);
		
		mniAccionesSalir.addActionListener(this);
		
		//contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.setVisible(true);		
		
		WelcomeLabel.setBounds(5, 6, 61, 16);
		contentPane.add(WelcomeLabel);
		
		TablaLabel.setBounds(229, 39, 89, 16);
		contentPane.add(TablaLabel);
		
		Actualizarbtn.setBounds(5, 62, 153, 29);
		Actualizarbtn.setFocusable(false);
		Actualizarbtn.addActionListener(this);
		contentPane.add(Actualizarbtn);
		
		TablaProfesorbtn.setBounds(213, 62, 117, 29);
		TablaProfesorbtn.setFocusable(false);
		TablaProfesorbtn.addActionListener(this);
		contentPane.add(TablaProfesorbtn);
		
		TablaAlumnobtn.setBounds(213, 103, 117, 29);
		TablaAlumnobtn.setFocusable(false);
		TablaAlumnobtn.addActionListener(this);
		contentPane.add(TablaAlumnobtn);
		
		Asignarbtn.setBounds(5, 103, 153, 29);
		Asignarbtn.setFocusable(false);
		Asignarbtn.addActionListener(this);
		contentPane.add(Asignarbtn);
		
		
		Logoutbtn.setBounds(180, 175, 117, 29);
		Logoutbtn.setFocusable(false);
		Logoutbtn.addActionListener(this);
		contentPane.add(Logoutbtn);
		
		NuevoUserbtn.setFocusable(false);
		NuevoUserbtn.setBounds(25, 175, 117, 29);
		NuevoUserbtn.addActionListener(this);
		contentPane.add(NuevoUserbtn);

	}
	
	@Override
	public void actionPerformed(ActionEvent e) {						 
		 if(e.getSource()==NuevoUserbtn) {
			Main.RegistrarUser();
			CerrarVentana();
		 }
		 if(e.getSource()==mniAccionesModificar||e.getSource()==Actualizarbtn) {
			 Main.Gestionar();
			 CerrarVentana();
		 }
		 if(e.getSource()==mniAccionesSalir||e.getSource()== Logoutbtn) {
			 Main.LogIn();
			 CerrarVentana();	
		 }
		 if(e.getSource()==mniAccionesAsignar || e.getSource()==Asignarbtn) {
			 Main.Asignar();
			 CerrarVentana();
		 }
		 if(e.getSource()==mntmProfeTabla || e.getSource()==TablaProfesorbtn) {
			 Main.ProfeTabla();
			 CerrarVentana();
		 }
		 if(e.getSource()==mntmAlumnoTabla || e.getSource()==TablaAlumnobtn) {
			 Main.AlumTabla();
			 CerrarVentana();
		 }
	}
	
	public void CerrarVentana() {
		Component com = SwingUtilities.getRoot(this);
		 ((Window) com).dispose();
	}
}
