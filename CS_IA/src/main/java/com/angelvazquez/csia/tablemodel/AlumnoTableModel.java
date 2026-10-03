package com.angelvazquez.csia.tablemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.model.Alumno;

/** Modelo Swing para alumnos del modelo de datos v2. */
public class AlumnoTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int COL_ID = 0;
    public static final int COL_NOMBRE = 1;
    public static final int COL_APELLIDO = 2;
    public static final int COL_DNI = 3;
    public static final int COL_EMAIL = 4;

    @Deprecated
    public static final int COL_NOMRE = COL_NOMBRE;

    private static final String[] CLAVES_COLUMNAS = {
            null, "table.name", "table.surname", null, null
    };

    private static final String[] COLUMNAS_FIJAS = {
            "ID", null, null, "DNI/NIE", "Email"
    };

    private static final Class<?>[] TIPOS = {
            Integer.class, String.class, String.class, String.class, String.class
    };

    // Modelo en memoria: sus operaciones notifican a Swing, pero no escriben en la base de datos.
    private final List<Alumno> data = new ArrayList<>();

    public int add(Alumno alumno) {
        data.add(alumno);
        int row = data.size() - 1;
        fireTableRowsInserted(row, row);
        return row;
    }

    /**
     * Reemplaza las filas y notifica una recarga completa; null deja el modelo vacío.
     * Copia las referencias de la colección, no las entidades que contiene.
     */
    public void setData(Collection<Alumno> alumnos) {
        data.clear();
        if (alumnos != null) {
            data.addAll(alumnos);
        }
        fireTableDataChanged();
    }

    /**
     * Sustituye la entidad de la fila del modelo y notifica a Swing el cambio.
     * La persistencia debe realizarse por separado.
     */
    public void updateRow(int row, Alumno alumno) {
        data.set(row, alumno);
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

    /**
     * Devuelve la misma entidad almacenada, sin crear una copia para edición.
     * El índice corresponde al modelo; una fila de JTable ordenada o filtrada debe convertirse.
     * Modificar la entidad no notifica por sí solo a Swing ni persiste el cambio.
     */
    public Alumno getAt(int row) {
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
