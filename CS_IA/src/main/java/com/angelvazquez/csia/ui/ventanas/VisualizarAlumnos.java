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
import com.angelvazquez.csia.model.Alumno;
import com.angelvazquez.csia.tablemodel.AlumnoTableModel;

public class VisualizarAlumnos extends VentanaSecundaria {
    private static final long serialVersionUID = 1L;
    private static final AlumnoTableModel modeloAlumno = new AlumnoTableModel();
    private final JTable tabla = new JTable(modeloAlumno);
    private final TableRowSorter<AlumnoTableModel> sorter = new TableRowSorter<>(modeloAlumno);
    private final JTextField nombre = new JTextField(10);
    private final JTextField apellido = new JTextField(10);
    private final JTextField dni = new JTextField(10);
    private final JTextField email = new JTextField(15);
    private final JButton agregar = new JButton("Agregar");
    private final JButton modificar = new JButton("Modificar");
    private final JButton eliminar = new JButton("Eliminar");
    private final JButton atras = new JButton("Atrás");
    private final AlumnoRepository repository;
    private final PersonasTableController controller;

    public VisualizarAlumnos() { this(null); }

    public VisualizarAlumnos(Window parent) {
        super(parent);
        DatabaseConnectionFactory factory = new DatabaseConnectionFactory();
        repository = new AlumnoRepository(factory, Main.getConfiguracion());
        controller = new PersonasTableController(repository,
                new ProfesorRepository(factory, Main.getConfiguracion()));
        configurarVentana();
        configurarAcciones();
        recargarDatos();
    }

    private void configurarVentana() {
        setTitle("Visualizar alumnos");
        setLayout(new BorderLayout());
        setBounds(100, 100, 1200, 500);
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
        Alumno a = seleccionado();
        boolean hay = a != null;
        modificar.setEnabled(hay); eliminar.setEnabled(hay);
        if (!hay) return;
        nombre.setText(a.GetNombre()); apellido.setText(a.GetApellido());
        dni.setText(a.GetDNI()); email.setText(a.getEmail());
    }

    private Alumno seleccionado() {
        int row = tabla.getSelectedRow();
        return row < 0 ? null : modeloAlumno.getAt(tabla.convertRowIndexToModel(row));
    }

    private boolean valido() {
        if (nombre.getText().isBlank() || apellido.getText().isBlank()
                || dni.getText().isBlank() || email.getText().isBlank()) {
            error("Nombre, apellido, DNI y email son obligatorios."); return false;
        }
        return true;
    }

    private void agregar() {
        if (!valido()) return;
        Alumno a = new Alumno(nombre.getText().trim(), apellido.getText().trim(),
                dni.getText().trim(), email.getText().trim());
        try {
            if (repository.existeDni(a.GetDNI())) { error("Ya existe un alumno con ese DNI."); return; }
            repository.agregar(a); recargarDatos(); limpiar();
        } catch (SQLException e) { error("No se ha podido agregar el alumno: " + e.getMessage()); }
    }

    private void modificar() {
        Alumno a = seleccionado(); if (a == null || !valido()) return;
        a.SetNombre(nombre.getText().trim()); a.SetApellido(apellido.getText().trim());
        a.DNI = dni.getText().trim(); a.setEmail(email.getText().trim());
        try { repository.modificar(a); recargarDatos(); limpiar(); }
        catch (SQLException e) { error("No se ha podido modificar el alumno: " + e.getMessage()); }
    }

    private void eliminar() {
        Alumno a = seleccionado();
        if (a == null || a.getDatabaseId() == null) { error("Selecciona un alumno para eliminar."); return; }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar al alumno seleccionado?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { repository.eliminar(a.getDatabaseId()); recargarDatos(); limpiar(); }
        catch (SQLException e) { error("No se ha podido eliminar el alumno. Comprueba si tiene asignaciones activas.\n" + e.getMessage()); }
    }

    private void limpiar() {
        tabla.clearSelection(); nombre.setText(""); apellido.setText(""); dni.setText(""); email.setText("");
        modificar.setEnabled(false); eliminar.setEnabled(false);
    }

    private void recargarDatos() {
        try { controller.cargarAlumnos(modeloAlumno); }
        catch (SQLException e) { error("No se han podido cargar los alumnos: " + e.getMessage()); }
    }

    private void error(String mensaje) { JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE); }
    public void RecargarVentana() { recargarDatos(); }
    public void CerrarVentana() { dispose(); }
}
