package com.angelvazquez.csia.ui.ventanas;

import java.awt.BorderLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import com.angelvazquez.csia.Main;
import com.angelvazquez.csia.controller.PersonasTableController;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.repository.AlumnoRepository;
import com.angelvazquez.csia.database.repository.ProfesorRepository;
import com.angelvazquez.csia.model.Profesor;
import com.angelvazquez.csia.tablemodel.ProfesorTableModel;

public class VisualizarProfesores extends VentanaSecundaria {
    private static final long serialVersionUID = 1L;
    private static final ProfesorTableModel modeloProfesor = new ProfesorTableModel();
    private final JTable tabla = new JTable(modeloProfesor);
    private final TableRowSorter<ProfesorTableModel> sorter = new TableRowSorter<>(modeloProfesor);
    private final JTextField nombre = new JTextField(10);
    private final JTextField apellido = new JTextField(10);
    private final JTextField dni = new JTextField(10);
    private final JTextField asignatura = new JTextField(12);
    private final JTextField email = new JTextField(15);
    private final JButton agregar = new JButton("Agregar");
    private final JButton modificar = new JButton("Modificar");
    private final JButton eliminar = new JButton("Eliminar");
    private final JButton atras = new JButton("Atrás");
    private final ProfesorRepository repository;
    private final PersonasTableController controller;

    public VisualizarProfesores() { this(null); }

    public VisualizarProfesores(Window parent) {
        super(parent);
        DatabaseConnectionFactory factory = new DatabaseConnectionFactory();
        repository = new ProfesorRepository(factory, Main.getConfiguracion());
        controller = new PersonasTableController(
                new AlumnoRepository(factory, Main.getConfiguracion()), repository);
        configurarVentana();
        configurarAcciones();
        recargarDatos();
    }

    private void configurarVentana() {
        setTitle("Visualizar profesores");
        setLayout(new BorderLayout());
        setBounds(100, 100, 1350, 500);
        tabla.setRowSorter(sorter);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JTextField filtro = new JTextField(15);
        filtro.getDocument().addDocumentListener(new DocumentListener() {
            private void actualizar() {
                String t = filtro.getText();
                sorter.setRowFilter(t.isBlank() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(t)));
            }
            @Override public void insertUpdate(DocumentEvent e) { actualizar(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizar(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizar(); }
        });
        add(filtro, BorderLayout.NORTH);
        JPanel p = new JPanel();
        p.add(new JLabel("Nombre:")); p.add(nombre);
        p.add(new JLabel("Apellido:")); p.add(apellido);
        p.add(new JLabel("DNI:")); p.add(dni);
        p.add(new JLabel("Asignatura:")); p.add(asignatura);
        p.add(new JLabel("Email:")); p.add(email);
        p.add(agregar); p.add(modificar); p.add(eliminar); p.add(atras);
        add(p, BorderLayout.SOUTH);
        modificar.setEnabled(false); eliminar.setEnabled(false);
        tabla.getSelectionModel().addListSelectionListener(e -> seleccionar());
    }

    private void configurarAcciones() {
        agregar.addActionListener(e -> agregar());
        modificar.addActionListener(e -> modificar());
        eliminar.addActionListener(e -> eliminar());
        atras.addActionListener(e -> volverAlPadre());
    }

    private void seleccionar() {
        Profesor p = seleccionado();
        boolean hay = p != null;
        modificar.setEnabled(hay); eliminar.setEnabled(hay);
        if (!hay) return;
        nombre.setText(p.GetNombre()); apellido.setText(p.GetApellido()); dni.setText(p.GetDNI());
        asignatura.setText(p.getAsignatura()); email.setText(p.getEmail());
    }

    private Profesor seleccionado() {
        int row = tabla.getSelectedRow();
        return row < 0 ? null : modeloProfesor.getAt(tabla.convertRowIndexToModel(row));
    }

    private boolean valido() {
        if (nombre.getText().isBlank() || apellido.getText().isBlank() || dni.getText().isBlank()
                || asignatura.getText().isBlank() || email.getText().isBlank()) {
            error("Nombre, apellido, DNI, asignatura y email son obligatorios."); return false;
        }
        return true;
    }

    private void agregar() {
        if (!valido()) return;
        Profesor p = new Profesor(nombre.getText().trim(), apellido.getText().trim(), dni.getText().trim(),
                asignatura.getText().trim(), email.getText().trim());
        try {
            if (repository.existeDni(p.GetDNI())) { error("Ya existe un profesor con ese DNI."); return; }
            repository.agregar(p); recargarDatos(); limpiar();
        } catch (SQLException e) { error("No se ha podido agregar el profesor: " + e.getMessage()); }
    }

    private void modificar() {
        Profesor p = seleccionado(); if (p == null || !valido()) return;
        p.SetNombre(nombre.getText().trim()); p.SetApellido(apellido.getText().trim()); p.DNI = dni.getText().trim();
        p.setAsignatura(asignatura.getText().trim()); p.setEmail(email.getText().trim());
        try { repository.modificar(p); recargarDatos(); limpiar(); }
        catch (SQLException e) { error("No se ha podido modificar el profesor: " + e.getMessage()); }
    }

    private void eliminar() {
        Profesor p = seleccionado();
        if (p == null || p.getDatabaseId() == null) { error("Selecciona un profesor para eliminar."); return; }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar al profesor seleccionado?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { repository.eliminar(p.getDatabaseId()); recargarDatos(); limpiar(); }
        catch (SQLException e) { error("No se ha podido eliminar el profesor. Comprueba si tiene asignaciones activas.\n" + e.getMessage()); }
    }

    private void limpiar() {
        tabla.clearSelection(); nombre.setText(""); apellido.setText(""); dni.setText("");
        asignatura.setText(""); email.setText(""); modificar.setEnabled(false); eliminar.setEnabled(false);
    }

    private void recargarDatos() {
        try { controller.cargarProfesores(modeloProfesor); }
        catch (SQLException e) { error("No se han podido cargar los profesores: " + e.getMessage()); }
    }

    private void error(String mensaje) { JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE); }
    public void RecargarVentana() { recargarDatos(); }
    public void CerrarVentana() { dispose(); }
}
