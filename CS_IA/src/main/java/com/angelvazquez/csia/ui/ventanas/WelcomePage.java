package com.angelvazquez.csia.ui.ventanas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.angelvazquez.csia.Main;
import com.angelvazquez.csia.i18n.I18n;

public class WelcomePage extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final JPanel contentPane = new JPanel();
    private final JLabel welcomeLabel = new JLabel(I18n.get("home.title"));
    private final JLabel tablaLabel = new JLabel(I18n.get("home.tables"));
    private final JButton tablaProfesorbtn = new JButton(I18n.get("home.teacher"));
    private final JButton tablaAlumnobtn = new JButton(I18n.get("home.student"));
    private final JButton tablaAsignacionesbtn = new JButton(I18n.get("home.assignments"));
    private final JButton asignarbtn = new JButton(I18n.get("home.assignTeacher"));
    private final JButton logoutbtn = new JButton(I18n.get("home.logout"));
    private final JButton nuevoUserbtn = new JButton(I18n.get("home.createUser"));
    private final JButton preferenciasbtn = new JButton(I18n.get("preferences.title"));
    private final JMenu mnAcciones = new JMenu(I18n.get("home.actions"));
    private final JMenuItem mniCrearUsuario = new JMenuItem(I18n.get("home.createUser"));
    private final JMenuItem mniAccionesSalir = new JMenuItem(I18n.get("app.exit"));
    private final JMenuItem mniAccionesAsignar = new JMenuItem(I18n.get("home.assign"));
    private final JMenuItem mniPreferencias = new JMenuItem(I18n.get("preferences.title"));
    private final JMenu mnTablas = new JMenu(I18n.get("home.tablesMenu"));
    private final JMenuItem mntmProfeTabla = new JMenuItem(I18n.get("home.teachers"));
    private final JMenuItem mntmAlumnoTabla = new JMenuItem(I18n.get("home.students"));

    private final JMenuItem mntmAsignacionesTabla = new JMenuItem(I18n.get("home.assignments"));

    public WelcomePage() {
        setTitle(I18n.get("home.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 430, 350);
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuBar.add(mnAcciones);
        mnAcciones.add(mniCrearUsuario);
        mnAcciones.add(new JSeparator());
        mnAcciones.add(mniAccionesAsignar);
        mnAcciones.add(new JSeparator());
        mniCrearUsuario.addActionListener(this);
        mnAcciones.add(mniPreferencias);
        mnAcciones.add(new JSeparator());
        mnAcciones.add(mniAccionesSalir);
        menuBar.add(mnTablas);
        mnTablas.add(mntmProfeTabla);
        mnTablas.add(new JSeparator());
        mnTablas.add(mntmAlumnoTabla);
        mnTablas.add(new JSeparator());
        mnTablas.add(mntmAsignacionesTabla);
        mntmAsignacionesTabla.addActionListener(this);
        mniAccionesAsignar.addActionListener(this);
        mniPreferencias.addActionListener(this);
        mniAccionesSalir.addActionListener(this);
        mntmProfeTabla.addActionListener(this);
        mntmAlumnoTabla.addActionListener(this);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        welcomeLabel.setBounds(5, 6, 100, 16); contentPane.add(welcomeLabel);
        tablaLabel.setBounds(225, 39, 130, 16); contentPane.add(tablaLabel);
        tablaProfesorbtn.setBounds(220, 62, 130, 29); tablaProfesorbtn.addActionListener(this); contentPane.add(tablaProfesorbtn);
        tablaAlumnobtn.setBounds(220, 103, 130, 29); tablaAlumnobtn.addActionListener(this); contentPane.add(tablaAlumnobtn);
        tablaAsignacionesbtn.setBounds(220, 144, 130, 29); tablaAsignacionesbtn.addActionListener(this); contentPane.add(tablaAsignacionesbtn);
        asignarbtn.setBounds(5, 62, 170, 29); asignarbtn.addActionListener(this); contentPane.add(asignarbtn);
        preferenciasbtn.setBounds(5, 103, 170, 29); preferenciasbtn.addActionListener(this); contentPane.add(preferenciasbtn);
        nuevoUserbtn.setBounds(25, 210, 140, 29); nuevoUserbtn.addActionListener(this); contentPane.add(nuevoUserbtn);
        logoutbtn.setBounds(190, 210, 140, 29); logoutbtn.addActionListener(this); contentPane.add(logoutbtn);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s == nuevoUserbtn || s == mniCrearUsuario) abrir(() -> Main.RegistrarUser(this));
        else if (s == preferenciasbtn || s == mniPreferencias) abrir(() -> Main.Preferencias(this));
        else if (s == logoutbtn) { Main.LogIn(); dispose(); }
        else if (s == mniAccionesSalir) { dispose(); System.exit(0); }
        else if (s == mniAccionesAsignar || s == asignarbtn) abrir(() -> Main.Asignar(this));
        else if (s == mntmProfeTabla || s == tablaProfesorbtn) abrir(() -> Main.ProfeTabla(this));
        else if (s == mntmAlumnoTabla || s == tablaAlumnobtn) abrir(() -> Main.AlumTabla(this));
        else if (s == mntmAsignacionesTabla || s == tablaAsignacionesbtn) abrir(() -> Main.AsignacionesTabla(this));
    }

    private void abrir(Runnable r) { setVisible(false); r.run(); }
    public void CerrarVentana() { setVisible(false); }
    @Deprecated public static void RestaurarVentana() { }
}
