package com.angelvazquez.csia.tablemodel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.angelvazquez.csia.model.Alumno;
import com.angelvazquez.csia.model.Persona;

/**
 * Modelo legado previo a Data Model v2.
 * Se mantiene temporalmente hasta migrar todas las ventanas y después se eliminará.
 */
@Deprecated
public class AlumnoTableModelOld extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int COL_NOMBRE = 0;
    public static final int COL_APELLIDO = 1;
    public static final int COL_DNI = 2;
    public static final int COL_FALTA = 3;
    public static final int COL_FBAJA = 4;

    private final String[] cols = {
            "Nombre", "Apellido", "DNI", "Fecha de alta", "Fecha de baja"
    };
    private final Class<?>[] types = {
            String.class, String.class, String.class, String.class, String.class
    };
    private final List<Alumno> data = new ArrayList<>();

    public int add(Alumno alumno) {
        data.add(alumno);
        int row = data.size() - 1;
        fireTableRowsInserted(row, row);
        return row;
    }

    public void updateRow(int row) {
        fireTableRowsUpdated(row, row);
    }

    public void removeAt(int row) {
        data.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public Persona getAt(int row) {
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int col) {
        return cols[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return types[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Alumno alumno = data.get(row);
        return switch (col) {
            case COL_NOMBRE -> alumno.GetNombre();
            case COL_APELLIDO -> alumno.GetApellido();
            case COL_DNI -> alumno.GetDNI();
            case COL_FALTA -> alumno.GetfAlta();
            case COL_FBAJA -> alumno.GetfBaja();
            default -> null;
        };
    }
}
