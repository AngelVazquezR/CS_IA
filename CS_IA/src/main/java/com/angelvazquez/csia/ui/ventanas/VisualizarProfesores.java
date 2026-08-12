package com.angelvazquez.csia.ui.ventanas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
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

public class VisualizarProfesores extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final ProfesorTableModel modeloProfesor = new ProfesorTableModel();

    private final JFrame frame = new JFrame("Visualizar profesores");
    private final JTable tabla = new JTable(modeloProfesor);
    private final TableRowSorter<ProfesorTableModel> sorter =
            new TableRowSorter<>(modeloProfesor);

    private final JTextField nombreField = new JTextField(10);
    private final JTextField apellidoField = new JTextField(10);
    private final JTextField dniField = new JTextField(10);
    private final JTextField asignaturaField = new JTextField(12);
    private final JTextField emailField = new JTextField(15);

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnAtras = new JButton("Atrás");

    private final ProfesorRepository profesorRepository;
    private final PersonasTableController controller;

    public VisualizarProfesores() {
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory();
        AlumnoRepository alumnoRepository = new AlumnoRepository(
                connectionFactory,
                Main.getConfiguracion()
        );
        profesorRepository = new ProfesorRepository(
                connectionFactory,
                Main.getConfiguracion()
        );
        controller = new PersonasTableController(
                alumnoRepository,
                profesorRepository
        );

        configurarVentana();
        configurarSeleccion();
        configurarAcciones();
        recargarDatos();
    }

    private void configurarVentana() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setBounds(100, 100, 1350, 500);

        tabla.setRowSorter(sorter);
        frame.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JTextField filtroField = new JTextField(15);
        frame.add(filtroField, BorderLayout.NORTH);
        instalarFiltro(filtroField);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Nombre:"));
        panel.add(nombreField);
        panel.add(new JLabel("Apellido:"));
        panel.add(apellidoField);
        panel.add(new JLabel("DNI:"));
        panel.add(dniField);
        panel.add(new JLabel("Asignatura:"));
        panel.add(asignaturaField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(btnAgregar);
        panel.add(btnModificar);
        panel.add(btnEliminar);
        panel.add(btnAtras);
        frame.add(panel, BorderLayout.SOUTH);

        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        frame.setVisible(true);
    }

    private void instalarFiltro(JTextField filtroField) {
        filtroField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar(filtroField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar(filtroField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrar(filtroField.getText());
            }
        });
    }

    private void filtrar(String texto) {
        if (texto == null || texto.isBlank()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                    RowFilter.regexFilter("(?i)" + Pattern.quote(texto))
            );
        }
    }

    private void configurarSeleccion() {
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            Profesor profesor = profesorSeleccionado();
            if (profesor == null) {
                btnModificar.setEnabled(false);
                btnEliminar.setEnabled(false);
                return;
            }

            cargarFormulario(profesor);
            btnModificar.setEnabled(true);
            btnEliminar.setEnabled(true);
        });
    }

    private void configurarAcciones() {
        btnAgregar.addActionListener(e -> agregarProfesor());
        btnModificar.addActionListener(e -> modificarProfesor());
        btnEliminar.addActionListener(e -> eliminarProfesor());
        btnAtras.addActionListener(e -> {
            Main.Welcome();
            CerrarVentana();
        });
    }

    private void agregarProfesor() {
        if (!formularioValido()) {
            return;
        }

        Profesor profesor = new Profesor(
                nombreField.getText().trim(),
                apellidoField.getText().trim(),
                dniField.getText().trim(),
                asignaturaField.getText().trim(),
                emailField.getText().trim()
        );

        try {
            if (profesorRepository.existeDni(profesor.GetDNI())) {
                mostrarError("Ya existe un profesor con ese DNI.");
                return;
            }

            profesorRepository.agregar(profesor);
            recargarDatos();
            limpiarFormulario();
        } catch (SQLException ex) {
            mostrarError("No se ha podido agregar el profesor: " + ex.getMessage());
        }
    }

    private void modificarProfesor() {
        Profesor profesor = profesorSeleccionado();
        if (profesor == null || !formularioValido()) {
            return;
        }

        profesor.SetNombre(nombreField.getText().trim());
        profesor.SetApellido(apellidoField.getText().trim());
        profesor.DNI = dniField.getText().trim();
        profesor.setAsignatura(asignaturaField.getText().trim());
        profesor.setEmail(emailField.getText().trim());

        try {
            profesorRepository.modificar(profesor);
            recargarDatos();
            limpiarFormulario();
        } catch (SQLException ex) {
            mostrarError("No se ha podido modificar el profesor: " + ex.getMessage());
        }
    }

    private void eliminarProfesor() {
        Profesor profesor = profesorSeleccionado();
        if (profesor == null || profesor.getDatabaseId() == null) {
            mostrarError("Selecciona un profesor para eliminar.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                frame,
                "¿Eliminar al profesor seleccionado?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            profesorRepository.eliminar(profesor.getDatabaseId());
            recargarDatos();
            limpiarFormulario();
        } catch (SQLException ex) {
            mostrarError(
                    "No se ha podido eliminar el profesor. "
                            + "Comprueba si tiene asignaciones activas.\n"
                            + ex.getMessage()
            );
        }
    }

    private boolean formularioValido() {
        if (nombreField.getText().isBlank()
                || apellidoField.getText().isBlank()
                || dniField.getText().isBlank()
                || asignaturaField.getText().isBlank()
                || emailField.getText().isBlank()) {
            mostrarError(
                    "Nombre, apellido, DNI, asignatura y email son obligatorios."
            );
            return false;
        }
        return true;
    }

    private Profesor profesorSeleccionado() {
        int viewRow = tabla.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return modeloProfesor.getAt(tabla.convertRowIndexToModel(viewRow));
    }

    private void cargarFormulario(Profesor profesor) {
        nombreField.setText(profesor.GetNombre());
        apellidoField.setText(profesor.GetApellido());
        dniField.setText(profesor.GetDNI());
        asignaturaField.setText(profesor.getAsignatura());
        emailField.setText(profesor.getEmail());
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        nombreField.setText("");
        apellidoField.setText("");
        dniField.setText("");
        asignaturaField.setText("");
        emailField.setText("");
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }

    private void recargarDatos() {
        try {
            controller.cargarProfesores(modeloProfesor);
        } catch (SQLException ex) {
            mostrarError("No se han podido cargar los profesores: " + ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                frame,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /** Compatibilidad temporal con ConectionSQL durante la migración. */
    @Deprecated
    public static void AddRow(
            String nombre,
            String apellido,
            String dni,
            String fAlta,
            String fBaja) {
        modeloProfesor.add(new Profesor(nombre, apellido, dni, "", ""));
    }

    public void RecargarVentana() {
        recargarDatos();
    }

    public void CerrarVentana() {
        Component com = SwingUtilities.getRoot(this);
        if (com instanceof Window window) {
            window.dispose();
        }
        frame.dispose();
    }
}
