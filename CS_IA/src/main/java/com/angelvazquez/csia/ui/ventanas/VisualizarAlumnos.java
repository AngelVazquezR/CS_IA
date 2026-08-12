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
import com.angelvazquez.csia.model.Alumno;
import com.angelvazquez.csia.tablemodel.AlumnoTableModel;

public class VisualizarAlumnos extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final AlumnoTableModel modeloAlumno = new AlumnoTableModel();

    private final JFrame frame = new JFrame("Visualizar alumnos");
    private final JTable tabla = new JTable(modeloAlumno);
    private final TableRowSorter<AlumnoTableModel> sorter =
            new TableRowSorter<>(modeloAlumno);

    private final JTextField nombreField = new JTextField(10);
    private final JTextField apellidoField = new JTextField(10);
    private final JTextField dniField = new JTextField(10);
    private final JTextField emailField = new JTextField(15);

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnAtras = new JButton("Atrás");

    private final AlumnoRepository alumnoRepository;
    private final PersonasTableController controller;

    public VisualizarAlumnos() {
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory();
        alumnoRepository = new AlumnoRepository(
                connectionFactory,
                Main.getConfiguracion()
        );
        ProfesorRepository profesorRepository = new ProfesorRepository(
                connectionFactory,
                Main.getConfiguracion()
        );
        controller = new PersonasTableController(
                alumnoRepository,
                profesorRepository
        );

        configurarVentana();
        configurarFiltro();
        configurarSeleccion();
        configurarAcciones();
        recargarDatos();
    }

    private void configurarVentana() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setBounds(100, 100, 1200, 500);

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

    private void configurarFiltro() {
        // El filtro se instala al crear el campo superior.
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

            Alumno alumno = alumnoSeleccionado();
            if (alumno == null) {
                btnModificar.setEnabled(false);
                btnEliminar.setEnabled(false);
                return;
            }

            cargarFormulario(alumno);
            btnModificar.setEnabled(true);
            btnEliminar.setEnabled(true);
        });
    }

    private void configurarAcciones() {
        btnAgregar.addActionListener(e -> agregarAlumno());
        btnModificar.addActionListener(e -> modificarAlumno());
        btnEliminar.addActionListener(e -> eliminarAlumno());
        btnAtras.addActionListener(e -> {
            Main.Welcome();
            CerrarVentana();
        });
    }

    private void agregarAlumno() {
        if (!formularioValido()) {
            return;
        }

        Alumno alumno = new Alumno(
                nombreField.getText().trim(),
                apellidoField.getText().trim(),
                dniField.getText().trim(),
                emailField.getText().trim()
        );

        try {
            if (alumnoRepository.existeDni(alumno.GetDNI())) {
                mostrarError("Ya existe un alumno con ese DNI.");
                return;
            }

            alumnoRepository.agregar(alumno);
            recargarDatos();
            limpiarFormulario();
        } catch (SQLException ex) {
            mostrarError("No se ha podido agregar el alumno: " + ex.getMessage());
        }
    }

    private void modificarAlumno() {
        Alumno alumno = alumnoSeleccionado();
        if (alumno == null || !formularioValido()) {
            return;
        }

        alumno.SetNombre(nombreField.getText().trim());
        alumno.SetApellido(apellidoField.getText().trim());
        alumno.DNI = dniField.getText().trim();
        alumno.setEmail(emailField.getText().trim());

        try {
            alumnoRepository.modificar(alumno);
            recargarDatos();
            limpiarFormulario();
        } catch (SQLException ex) {
            mostrarError("No se ha podido modificar el alumno: " + ex.getMessage());
        }
    }

    private void eliminarAlumno() {
        Alumno alumno = alumnoSeleccionado();
        if (alumno == null || alumno.getDatabaseId() == null) {
            mostrarError("Selecciona un alumno para eliminar.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                frame,
                "¿Eliminar al alumno seleccionado?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            alumnoRepository.eliminar(alumno.getDatabaseId());
            recargarDatos();
            limpiarFormulario();
        } catch (SQLException ex) {
            mostrarError(
                    "No se ha podido eliminar el alumno. "
                            + "Comprueba si tiene asignaciones activas.\n"
                            + ex.getMessage()
            );
        }
    }

    private boolean formularioValido() {
        if (nombreField.getText().isBlank()
                || apellidoField.getText().isBlank()
                || dniField.getText().isBlank()
                || emailField.getText().isBlank()) {
            mostrarError("Nombre, apellido, DNI y email son obligatorios.");
            return false;
        }
        return true;
    }

    private Alumno alumnoSeleccionado() {
        int viewRow = tabla.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return modeloAlumno.getAt(tabla.convertRowIndexToModel(viewRow));
    }

    private void cargarFormulario(Alumno alumno) {
        nombreField.setText(alumno.GetNombre());
        apellidoField.setText(alumno.GetApellido());
        dniField.setText(alumno.GetDNI());
        emailField.setText(alumno.getEmail());
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        nombreField.setText("");
        apellidoField.setText("");
        dniField.setText("");
        emailField.setText("");
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }

    private void recargarDatos() {
        try {
            controller.cargarAlumnos(modeloAlumno);
        } catch (SQLException ex) {
            mostrarError("No se han podido cargar los alumnos: " + ex.getMessage());
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
    public static void AddRow(String nombre, String apellido, String dni, String prof) {
        modeloAlumno.add(new Alumno(nombre, apellido, dni, ""));
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
