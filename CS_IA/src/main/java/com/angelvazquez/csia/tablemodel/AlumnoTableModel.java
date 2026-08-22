package com.angelvazquez.csia.tablemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.angelvazquez.csia.model.Alumno;

/** Modelo Swing para alumnos del modelo de datos v2. */
public class AlumnoTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int COL_ID = 0;
    public static final int COL_NOMBRE = 1;
    public static final int COL_APELLIDO = 2;
    public static final int COL_DNI = 3;
    public static final int COL_EMAIL = 4;

    /** Alias temporal para código antiguo que todavía usa el typo histórico. */
    @Deprecated
    public static final int COL_NOMRE = COL_NOMBRE;

    private static final String[] COLUMNAS = {
            "ID", "Nombre", "Apellido", "DNI", "Email"
    };

    private static final Class<?>[] TIPOS = {
            Integer.class, String.class, String.class, String.class, String.class
    };

    private final List<Alumno> data = new ArrayList<>();

    public int add(Alumno alumno) {
        data.add(alumno);
        int row = data.size() - 1;
        fireTableRowsInserted(row, row);
        return row;
    }

    /** Sustituye todos los datos, pensado para cargas procedentes del repository. */
    public void setData(Collection<Alumno> alumnos) {
        data.clear();
        if (alumnos != null) {
            data.addAll(alumnos);
        }
        fireTableDataChanged();
    }

    public void updateRow(int row, Alumno alumno) {
        data.set(row, alumno);
        fireTableRowsUpdated(row, row);
    }

    /** API antigua conservada temporalmente: notifica que una fila ha cambiado. */
    @Deprecated
    public void updateRow(int row) {
        fireTableRowsUpdated(row, row);
    }

    public void removeAt(int row) {
        data.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public Alumno getAt(int row) {
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMNAS[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return TIPOS[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Alumno alumno = data.get(row);
        return switch (col) {
            case COL_ID -> alumno.getDatabaseId();
            case COL_NOMBRE -> alumno.GetNombre();
            case COL_APELLIDO -> alumno.GetApellido();
            case COL_DNI -> alumno.GetDNI();
            case COL_EMAIL -> alumno.getEmail();
            default -> null;
        };
    }
}
