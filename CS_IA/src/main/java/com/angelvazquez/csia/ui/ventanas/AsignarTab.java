package com.angelvazquez.csia.ui.ventanas;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.angelvazquez.csia.Main;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.repository.AlumnoRepository;
import com.angelvazquez.csia.database.repository.AsignacionRepository;
import com.angelvazquez.csia.database.repository.ProfesorRepository;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.model.Alumno;
import com.angelvazquez.csia.model.Asignacion;
import com.angelvazquez.csia.model.Profesor;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;

public class AsignarTab extends VentanaSecundaria {

    private static final long serialVersionUID = 1L;

    private final JComboBox<Object> profesorCombo = new JComboBox<>();
    private final JComboBox<Object> alumnoCombo = new JComboBox<>();
    private final JComboBox<DiaSemana> diaCombo = new JComboBox<>(DiaSemana.values());
    private final TimePicker horaInicioPicker = new TimePicker();
    private final DatePicker fechaInicioPicker = new DatePicker();
    private final DatePicker fechaFinPicker = new DatePicker();

    private final JButton asignarButton = new JButton(I18n.get("home.assign"));
    private final JButton atrasButton = new JButton(I18n.get("app.back"));

    private final Integer asignacionId;
    private final Runnable alGuardar;
    private final AlumnoRepository alumnoRepository;
    private final ProfesorRepository profesorRepository;
    private final AsignacionRepository asignacionRepository;

    public AsignarTab() {
        this(null);
    }

    public AsignarTab(Window parent) {
        this(parent, null, null);
    }

    public AsignarTab(Window parent, Asignacion seleccionada, Runnable alGuardar) {
        super(parent);
        this.asignacionId = seleccionada == null ? null : seleccionada.getId();
        this.alGuardar = alGuardar;

        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory();
        alumnoRepository = new AlumnoRepository(connectionFactory, Main.getConfiguracion());
        profesorRepository = new ProfesorRepository(connectionFactory, Main.getConfiguracion());
        asignacionRepository = new AsignacionRepository(connectionFactory, Main.getConfiguracion());

        configurarVentana();
        cargarPersonas();
        configurarAcciones();
        if (seleccionada != null) {
            setTitle(I18n.get("assign.editTitle", asignacionId));
            asignarButton.setText(I18n.get("assign.saveChanges"));
            seleccionarPersona(profesorCombo, seleccionada.getProfesorId());
            seleccionarPersona(alumnoCombo, seleccionada.getAlumnoId());
            diaCombo.setSelectedIndex(seleccionada.getDiaSemana() - 1);
            horaInicioPicker.setTime(seleccionada.getHoraInicio());
            fechaInicioPicker.setDate(seleccionada.getFechaInicio());
            fechaFinPicker.setDate(seleccionada.getFechaFin());
        }
    }

    private void seleccionarPersona(JComboBox<Object> combo, Integer id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i) instanceof OpcionPersona persona && persona.id().equals(id)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        // No sustituir silenciosamente una persona que ya no existe.
        asignarButton.setEnabled(false);
        mostrarError(I18n.get("assign.personMissing", id));
    }

    private void configurarVentana() {
        setTitle(I18n.get("assign.title"));
        setBounds(100, 100, 620, 360);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(contentPane);

        JPanel formulario = new JPanel(new GridLayout(6, 2, 8, 8));
        formulario.add(new JLabel(I18n.get("assign.teacher")));
        formulario.add(profesorCombo);
        formulario.add(new JLabel(I18n.get("assign.student")));
        formulario.add(alumnoCombo);
        formulario.add(new JLabel(I18n.get("assign.day")));
        formulario.add(diaCombo);
        formulario.add(new JLabel(I18n.get("assign.startTime")));
        formulario.add(horaInicioPicker);
        formulario.add(new JLabel(I18n.get("assign.startDate")));
        formulario.add(fechaInicioPicker);
        formulario.add(new JLabel(I18n.get("assign.endDate")));
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
        profesorCombo.addItem(I18n.get("assign.selectTeacher"));
        alumnoCombo.addItem(I18n.get("assign.selectStudent"));

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
            mostrarError(I18n.get("assign.peopleLoadError", ex.getMessage()));
        }
    }

    private void configurarAcciones() {
        atrasButton.addActionListener(e -> volverAlPadre());
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
            mostrarError(I18n.get("assign.personRequired"));
            return;
        }

        if (horaInicio == null || fechaInicio == null || fechaFin == null || dia == null) {
            mostrarError(I18n.get("assign.scheduleRequired"));
            return;
        }

        if (fechaFin.isBefore(fechaInicio)) {
            mostrarError(I18n.get("assign.invalidDates"));
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
            int id;
            if (asignacionId == null) {
                id = asignacionRepository.agregar(asignacion);
            } else {
                asignacion.setId(asignacionId);
                if (!asignacionRepository.modificar(asignacion)) {
                    mostrarError(I18n.get("assignments.notFound"));
                    return;
                }
                id = asignacionId;
            }
            JOptionPane.showMessageDialog(
                    this,
                    I18n.get(asignacionId == null ? "assign.success" : "assign.updated", id),
                    I18n.get("assign.dialogTitle"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            if (alGuardar != null) {
                volverAlPadre();
                alGuardar.run();
            }
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        } catch (SQLException ex) {
            mostrarError(I18n.get("assign.saveError", ex.getMessage()));
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                I18n.get("app.error"),
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void CerrarVentana() {
        dispose();
    }

    @Deprecated
    public static void AddAlumPop(String alum) {
    }

    @Deprecated
    public static void AddProfePop(String profe) {
    }

    private record OpcionPersona(Integer id, String nombre, String dni) {
        @Override
        public String toString() {
            return nombre + " (" + dni + ")";
        }
    }

    private enum DiaSemana {
        LUNES(1, "day.monday"),
        MARTES(2, "day.tuesday"),
        MIERCOLES(3, "day.wednesday"),
        JUEVES(4, "day.thursday"),
        VIERNES(5, "day.friday"),
        SABADO(6, "day.saturday"),
        DOMINGO(7, "day.sunday");

        private final int numero;
        private final String clave;

        DiaSemana(int numero, String clave) {
            this.numero = numero;
            this.clave = clave;
        }

        int numero() {
            return numero;
        }

        @Override
        public String toString() {
            return I18n.get(clave);
        }
    }
}
