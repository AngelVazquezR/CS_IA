package com.angelvazquez.csia.ui.ventanas;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.angelvazquez.csia.Main;

public class WelcomePage extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final JPanel contentPane = new JPanel();
    private static final Component comp = SwingUtilities.getRoot(contentPane);

    private final JLabel welcomeLabel = new JLabel("Home");
    private final JLabel tablaLabel = new JLabel("Ver tabla de:");

    private final JButton tablaProfesorbtn = new JButton("Tutor");
    private final JButton tablaAlumnobtn = new JButton("Alumno");
    private final JButton asignarbtn = new JButton("Asignar profesor");
    private final JButton logoutbtn = new JButton("Cerrar sesión");
    private final JButton nuevoUserbtn = new JButton("Crear usuario");

    private final JMenu mnAcciones = new JMenu("Acciones");
    private final JMenuItem mniAccionesSalir = new JMenuItem("Salir");
    private final JMenuItem mniAccionesAsignar = new JMenuItem("Asignar");
    private final JMenu mnTablas = new JMenu("Tablas");
    private final JMenuItem mntmProfeTabla = new JMenuItem("Profesores");
    private final JMenuItem mntmAlumnoTabla = new JMenuItem("Alumnos");

    public WelcomePage() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 402, 319);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        menuBar.add(mnAcciones);
        mnAcciones.add(mniAccionesAsignar);
        mnAcciones.add(new JSeparator());
        mnAcciones.add(mniAccionesSalir);

        menuBar.add(mnTablas);
        mnTablas.add(mntmProfeTabla);
        mnTablas.add(new JSeparator());
        mnTablas.add(mntmAlumnoTabla);

        mniAccionesAsignar.addActionListener(this);
        mniAccionesSalir.addActionListener(this);
        mntmProfeTabla.addActionListener(this);
        mntmAlumnoTabla.addActionListener(this);

        contentPane.removeAll();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        contentPane.setVisible(true);

        welcomeLabel.setBounds(5, 6, 61, 16);
        contentPane.add(welcomeLabel);

        tablaLabel.setBounds(229, 39, 89, 16);
        contentPane.add(tablaLabel);

        tablaProfesorbtn.setBounds(213, 62, 117, 29);
        tablaProfesorbtn.setFocusable(false);
        tablaProfesorbtn.addActionListener(this);
        contentPane.add(tablaProfesorbtn);

        tablaAlumnobtn.setBounds(213, 103, 117, 29);
        tablaAlumnobtn.setFocusable(false);
        tablaAlumnobtn.addActionListener(this);
        contentPane.add(tablaAlumnobtn);

        asignarbtn.setBounds(5, 62, 153, 29);
        asignarbtn.setFocusable(false);
        asignarbtn.addActionListener(this);
        contentPane.add(asignarbtn);

        nuevoUserbtn.setFocusable(false);
        nuevoUserbtn.setBounds(25, 175, 117, 29);
        nuevoUserbtn.addActionListener(this);
        contentPane.add(nuevoUserbtn);

        logoutbtn.setBounds(180, 175, 117, 29);
        logoutbtn.setFocusable(false);
        logoutbtn.addActionListener(this);
        contentPane.add(logoutbtn);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nuevoUserbtn) {
            Main.RegistrarUser();
        } else if (e.getSource() == mniAccionesSalir || e.getSource() == logoutbtn) {
            Main.LogIn();
            CerrarVentana();
        } else if (e.getSource() == mniAccionesAsignar || e.getSource() == asignarbtn) {
            Main.Asignar();
        } else if (e.getSource() == mntmProfeTabla || e.getSource() == tablaProfesorbtn) {
            Main.ProfeTabla();
            CerrarVentana();
        } else if (e.getSource() == mntmAlumnoTabla || e.getSource() == tablaAlumnobtn) {
            Main.AlumTabla();
            CerrarVentana();
        }
    }

    public void CerrarVentana() {
        setVisible(false);
    }

    public static void RestaurarVentana() {
        if (comp instanceof Window window) {
            window.setVisible(true);
        }
    }
}
