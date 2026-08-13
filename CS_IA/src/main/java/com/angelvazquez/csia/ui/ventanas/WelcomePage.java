package com.angelvazquez.csia.ui.ventanas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.angelvazquez.csia.Main;

public class WelcomePage extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final JPanel contentPane = new JPanel();
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
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        welcomeLabel.setBounds(5, 6, 61, 16); contentPane.add(welcomeLabel);
        tablaLabel.setBounds(229, 39, 89, 16); contentPane.add(tablaLabel);
        tablaProfesorbtn.setBounds(213, 62, 117, 29); tablaProfesorbtn.addActionListener(this); contentPane.add(tablaProfesorbtn);
        tablaAlumnobtn.setBounds(213, 103, 117, 29); tablaAlumnobtn.addActionListener(this); contentPane.add(tablaAlumnobtn);
        asignarbtn.setBounds(5, 62, 153, 29); asignarbtn.addActionListener(this); contentPane.add(asignarbtn);
        nuevoUserbtn.setBounds(25, 175, 117, 29); nuevoUserbtn.addActionListener(this); contentPane.add(nuevoUserbtn);
        logoutbtn.setBounds(180, 175, 117, 29); logoutbtn.addActionListener(this); contentPane.add(logoutbtn);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s == nuevoUserbtn) abrir(() -> Main.RegistrarUser(this));
        else if (s == logoutbtn) { Main.LogIn(); dispose(); }
        else if (s == mniAccionesSalir) { dispose(); System.exit(0); }
        else if (s == mniAccionesAsignar || s == asignarbtn) abrir(() -> Main.Asignar(this));
        else if (s == mntmProfeTabla || s == tablaProfesorbtn) abrir(() -> Main.ProfeTabla(this));
        else if (s == mntmAlumnoTabla || s == tablaAlumnobtn) abrir(() -> Main.AlumTabla(this));
    }

    private void abrir(Runnable r) { setVisible(false); r.run(); }
    public void CerrarVentana() { setVisible(false); }
    @Deprecated public static void RestaurarVentana() { }
}
