package com.angelvazquez.csia.tablemodel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.model.Asignacion;

/** Tabla de consulta: conserva fechas y horas tipadas para ordenar correctamente. */
public class AsignacionTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNAS = {"assignments.id", "home.teacher", "home.student",
            "assignments.day", "assignments.time", "assignments.startDate", "assignments.endDate"};
    private static final String[] DIAS = {"day.monday", "day.tuesday", "day.wednesday",
            "day.thursday", "day.friday", "day.saturday", "day.sunday"};
    private static final Class<?>[] TIPOS = {Integer.class, String.class, String.class,
            String.class, LocalTime.class, LocalDate.class, LocalDate.class};
    private List<Asignacion> datos = List.of();
    private Map<Integer, String> profesores = Map.of();
    private Map<Integer, String> alumnos = Map.of();

    public void setData(List<Asignacion> datos, Map<Integer, String> profesores, Map<Integer, String> alumnos) {
        this.datos = List.copyOf(datos);
        this.profesores = Map.copyOf(profesores);
        this.alumnos = Map.copyOf(alumnos);
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return datos.size(); }
    @Override public int getColumnCount() { return COLUMNAS.length; }
    @Override public String getColumnName(int col) { return I18n.get(COLUMNAS[col]); }
    @Override public Class<?> getColumnClass(int col) { return TIPOS[col]; }
    @Override public boolean isCellEditable(int row, int col) { return false; }
    @Override public Object getValueAt(int row, int col) {
        Asignacion a = datos.get(row);
        return switch (col) {
            case 0 -> a.getId();
            case 1 -> profesores.getOrDefault(a.getProfesorId(), "ID " + a.getProfesorId());
            case 2 -> alumnos.getOrDefault(a.getAlumnoId(), "ID " + a.getAlumnoId());
            case 3 -> I18n.get(DIAS[a.getDiaSemana() - 1]);
            case 4 -> a.getHoraInicio();
            case 5 -> a.getFechaInicio();
            case 6 -> a.getFechaFin();
            default -> null;
        };
    }
}
