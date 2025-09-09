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
	JLabel AddLabel = new JLabel("Añadir");
	JLabel ModificarLabel = new JLabel("Modificar");
	JLabel AsignarLabel = new JLabel("Asignar");
	
	JButton AddTutor = new JButton("Tutor");
	JButton AddAlumno = new JButton("Alumno");
	JButton ModTutor = new JButton("Tutor");
	JButton ModAlumno = new JButton("Alumno");
	JButton AsigTutor = new JButton("Tutor");
	JButton AsigAlumno = new JButton("Alumno");
	JButton Logout = new JButton("Cerrar sesión");
	JButton NuevoUser = new JButton("Nuevo User");
	private final JMenu mnAcciones = new JMenu("Acciones");
	private final JMenuItem mniAccionesModificar = new JMenuItem("Modificar");
	private final JMenuItem mniAccionesSalir = new JMenuItem("Salir");
	private final JSeparator mniAccionesSeparador1 = new JSeparator();
	private final JMenuItem mniAccionesAsignar = new JMenuItem("Asignar");
	private final JSeparator mniAccionesSeparador2 = new JSeparator();

	
	public WelcomePage() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 600);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		menuBar.add(mnAcciones);
		
		mnAcciones.add(mniAccionesModificar);
		mniAccionesModificar.addActionListener(this);
		
		mnAcciones.add(mniAccionesSeparador1);
		
		mnAcciones.add(mniAccionesSalir);
		mniAccionesSalir.addActionListener(this);
		
		mnAcciones.add(mniAccionesSeparador2);
		
		mnAcciones.add(mniAccionesAsignar);
		mniAccionesAsignar.addActionListener(this);
		
		//contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.setVisible(true);		
		
		WelcomeLabel.setBounds(129, 180, 61, 16);
		contentPane.add(WelcomeLabel);
		
		AddLabel.setBounds(16, 34, 61, 16);
		contentPane.add(AddLabel);
		
		ModificarLabel.setBounds(150, 34, 61, 16);
		contentPane.add(ModificarLabel);
		
		AsignarLabel.setBounds(289, 34, 61, 16);
		contentPane.add(AsignarLabel);
		
		AddTutor.setBounds(0, 62, 117, 29);
		AddTutor.setFocusable(false);
		AddTutor.addActionListener(this);
		contentPane.add(AddTutor);
		
		AddAlumno.setBounds(0, 103, 117, 29);
		AddAlumno.setFocusable(false);
		AddAlumno.addActionListener(this);
		contentPane.add(AddAlumno);
		
		ModTutor.setBounds(150, 62, 117, 29);
		ModTutor.setFocusable(false);
		ModTutor.addActionListener(this);
		contentPane.add(ModTutor);
		
		ModAlumno.setBounds(150, 103, 117, 29);
		ModAlumno.setFocusable(false);
		ModAlumno.addActionListener(this);
		contentPane.add(ModAlumno);
		
		AsigTutor.setBounds(289, 62, 117, 29);
		AsigTutor.setFocusable(false);
		AsigTutor.addActionListener(this);
		contentPane.add(AsigTutor);
		
		AsigAlumno.setBounds(289, 103, 117, 29);
		AsigAlumno.setFocusable(false);
		AsigAlumno.addActionListener(this);
		contentPane.add(AsigAlumno);
		
		
		Logout.setBounds(0, 237, 117, 29);
		Logout.setFocusable(false);
		Logout.addActionListener(this);
		contentPane.add(Logout);
		
		NuevoUser.setFocusable(false);
		NuevoUser.setBounds(0, 175, 117, 29);
		NuevoUser.addActionListener(this);
		contentPane.add(NuevoUser);

	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		 if(e.getSource()== Logout) {	 
			 Main.LogIn();
			 CerrarVentana();			 
		}
		 
		 if(e.getSource()==ModTutor) {
			 ModificarTab modificarTab = new ModificarTab();
			 modificarTab.setVisible(true);
			 CerrarVentana();			 
		 }
		 
		 if(e.getSource()==NuevoUser) {
			RegistarTab registrarTab = new RegistarTab();
			registrarTab.setVisible(true);
			CerrarVentana();
		 }
		 if(e.getSource()==mniAccionesModificar) {
			 Main.Gestionar();
			 CerrarVentana();
		 }
		 if(e.getSource()==mniAccionesSalir) {
			 Main.LogIn();
			 CerrarVentana();	
		 }
		 if(e.getSource()==mniAccionesAsignar) {
			 Main.Asignar();
			 CerrarVentana();
		 }
	}
	
	public void CerrarVentana() {
		Component com = SwingUtilities.getRoot(this);
		 ((Window) com).dispose();
	}
}
