package com.angelvazquez.csia.ui.ventanas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.angelvazquez.csia.Main;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.repository.AlumnoRepository;
import com.angelvazquez.csia.database.repository.AsignacionRepository;
import com.angelvazquez.csia.database.repository.ProfesorRepository;
import com.angelvazquez.csia.model.Alumno;
import com.angelvazquez.csia.model.Asignacion;
import com.angelvazquez.csia.model.Profesor;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;

public class AsignarTab extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JComboBox<Object> profesorCombo = new JComboBox<>();
    private final JComboBox<Object> alumnoCombo = new JComboBox<>();
    private final JComboBox<DiaSemana> diaCombo = new JComboBox<>(DiaSemana.values());
    private final TimePicker horaInicioPicker = new TimePicker();
    private final DatePicker fechaInicioPicker = new DatePicker();
    private final DatePicker fechaFinPicker = new DatePicker();

    private final JButton asignarButton = new JButton("Asignar");
    private final JButton atrasButton = new JButton("Atrás");

    private final AlumnoRepository alumnoRepository;
    private final ProfesorRepository profesorRepository;
    private final AsignacionRepository asignacionRepository;

    public AsignarTab() {
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory();
        alumnoRepository = new AlumnoRepository(connectionFactory, Main.getConfiguracion());
        profesorRepository = new ProfesorRepository(connectionFactory, Main.getConfiguracion());
        asignacionRepository = new AsignacionRepository(connectionFactory, Main.getConfiguracion());

        configurarVentana();
        cargarPersonas();
        configurarAcciones();
    }

    private void configurarVentana() {
        setTitle("Asignar profesor a alumno");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 620, 360);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(contentPane);

        JPanel formulario = new JPanel(new GridLayout(6, 2, 8, 8));
        formulario.add(new JLabel("Profesor:"));
        formulario.add(profesorCombo);
        formulario.add(new JLabel("Alumno:"));
        formulario.add(alumnoCombo);
        formulario.add(new JLabel("Día de la semana:"));
        formulario.add(diaCombo);
        formulario.add(new JLabel("Hora de inicio:"));
        formulario.add(horaInicioPicker);
        formulario.add(new JLabel("Fecha de inicio:"));
        formulario.add(fechaInicioPicker);
        formulario.add(new JLabel("Fecha de fin:"));
        formulario.add(fechaFinPicker);
        contentPane.add(formulario, BorderLayout.CENTER);

        JPanel botones = new JPanel();
        botones.add(atrasButton);
        botones.add(asignarButton);
        contentPane.add(botones, BorderLayout.SOUTH);

        fechaInicioPicker.setDate(LocalDate.now());
        fechaFinPicker.setDate(LocalDate.now());
    }

    private void cargarPersonas() {
        profesorCombo.removeAllItems();
        alumnoCombo.removeAllItems();
        profesorCombo.addItem("Selecciona un profesor");
        alumnoCombo.addItem("Selecciona un alumno");

        try {
            for (Profesor profesor : profesorRepository.listar()) {
                profesorCombo.addItem(new OpcionPersona(
                        profesor.getDatabaseId(),
                        profesor.GetNombre() + " " + profesor.GetApellido(),
                        profesor.GetDNI()
                ));
            }

            for (Alumno alumno : alumnoRepository.listar()) {
                alumnoCombo.addItem(new OpcionPersona(
                        alumno.getDatabaseId(),
                        alumno.GetNombre() + " " + alumno.GetApellido(),
                        alumno.GetDNI()
                ));
            }
        } catch (SQLException ex) {
            mostrarError("No se han podido cargar alumnos y profesores: " + ex.getMessage());
        }
    }

    private void configurarAcciones() {
        atrasButton.addActionListener(e -> {
            WelcomePage.RestaurarVentana();
            CerrarVentana();
        });
        asignarButton.addActionListener(e -> guardarAsignacion());
    }

    private void guardarAsignacion() {
        Object profesorSeleccionado = profesorCombo.getSelectedItem();
        Object alumnoSeleccionado = alumnoCombo.getSelectedItem();
        LocalTime horaInicio = horaInicioPicker.getTime();
        LocalDate fechaInicio = fechaInicioPicker.getDate();
        LocalDate fechaFin = fechaFinPicker.getDate();
        DiaSemana dia = (DiaSemana) diaCombo.getSelectedItem();

        if (!(profesorSeleccionado instanceof OpcionPersona profesor)
                || !(alumnoSeleccionado instanceof OpcionPersona alumno)) {
            mostrarError("Selecciona un profesor y un alumno.");
            return;
        }

        if (horaInicio == null || fechaInicio == null || fechaFin == null || dia == null) {
            mostrarError("Día, hora de inicio y fechas son obligatorios.");
            return;
        }

        Asignacion asignacion = new Asignacion(
                profesor.id(),
                alumno.id(),
                dia.numero(),
                horaInicio,
                fechaInicio,
                fechaFin
        );

        try {
            int id = asignacionRepository.agregar(asignacion);
            JOptionPane.showMessageDialog(
                    this,
                    "Asignación creada correctamente (ID " + id + ").",
                    "Asignación",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        } catch (SQLException ex) {
            mostrarError("No se ha podido guardar la asignación: " + ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void CerrarVentana() {
        Component com = SwingUtilities.getRoot(this);
        if (com instanceof Window window) {
            window.dispose();
        } else {
            dispose();
        }
    }

    /** Compatibilidad temporal mientras ConectionSQL termina de retirarse. */
    @Deprecated
    public static void AddAlumPop(String alum) {
        // Ya no se usa: los alumnos se cargan por AlumnoRepository con STUDENT_ID.
    }

    /** Compatibilidad temporal mientras ConectionSQL termina de retirarse. */
    @Deprecated
    public static void AddProfePop(String profe) {
        // Ya no se usa: los profesores se cargan por ProfesorRepository con TEACHER_ID.
    }

    private record OpcionPersona(Integer id, String nombre, String dni) {
        @Override
        public String toString() {
            return nombre + " (" + dni + ")";
        }
    }

    private enum DiaSemana {
        LUNES(1, "Lunes"),
        MARTES(2, "Martes"),
        MIERCOLES(3, "Miércoles"),
        JUEVES(4, "Jueves"),
        VIERNES(5, "Viernes"),
        SABADO(6, "Sábado"),
        DOMINGO(7, "Domingo");

        private final int numero;
        private final String etiqueta;

        DiaSemana(int numero, String etiqueta) {
            this.numero = numero;
            this.etiqueta = etiqueta;
        }

        int numero() {
            return numero;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }
}
