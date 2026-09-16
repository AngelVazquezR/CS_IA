package com.angelvazquez.csia.tablemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.model.Profesor;

/** Modelo Swing para profesores del modelo de datos v2. */
public class ProfesorTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int COL_ID = 0;
    public static final int COL_NOMBRE = 1;
    public static final int COL_APELLIDO = 2;
    public static final int COL_DNI = 3;
    public static final int COL_ASIGNATURA = 4;
    public static final int COL_EMAIL = 5;

    private static final String[] CLAVES_COLUMNAS = {
            null, "table.name", "table.surname", null, "table.subject", null
    };

    private static final String[] COLUMNAS_FIJAS = {
            "ID", null, null, "DNI", null, "Email"
    };

    private static final Class<?>[] TIPOS = {
            Integer.class, String.class, String.class,
            String.class, String.class, String.class
    };

    private final List<Profesor> data = new ArrayList<>();

    public int add(Profesor profesor) {
        data.add(profesor);
        int row = data.size() - 1;
        fireTableRowsInserted(row, row);
        return row;
    }

    public void setData(Collection<Profesor> profesores) {
        data.clear();
        if (profesores != null) {
            data.addAll(profesores);
        }
        fireTableDataChanged();
    }

    public void updateRow(int row, Profesor profesor) {
        data.set(row, profesor);
        fireTableRowsUpdated(row, row);
    }

    @Deprecated
    public void updateRow(int row) {
        fireTableRowsUpdated(row, row);
    }

    public void removeAt(int row) {
        data.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public Profesor getAt(int row) {
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return CLAVES_COLUMNAS.length;
    }

    @Override
    public String getColumnName(int col) {
        String clave = CLAVES_COLUMNAS[col];
        return clave == null ? COLUMNAS_FIJAS[col] : I18n.get(clave);
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
        Profesor profesor = data.get(row);
        return switch (col) {
            case COL_ID -> profesor.getDatabaseId();
            case COL_NOMBRE -> profesor.GetNombre();
            case COL_APELLIDO -> profesor.GetApellido();
            case COL_DNI -> profesor.GetDNI();
            case COL_ASIGNATURA -> profesor.getAsignatura();
            case COL_EMAIL -> profesor.getEmail();
            default -> null;
        };
    }
}
