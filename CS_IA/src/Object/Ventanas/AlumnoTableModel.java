package Object.Ventanas;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import Object.Persona;
import Object.Alumno;

public class AlumnoTableModel extends  AbstractTableModel{

	public static final int COL_NOMBRE=0;
	public static final int COL_APELLIDO=1;
	public static final int COL_DNI=2;
	public static final int COL_FALTA=3;
	public static final int COL_FBAJA=4;
	
	private final String[] cols = new String[]{"Nombre","Apellido","DNI","Fecha de alta","Fechas de baja"};
	private final Class<?>[] types = {String.class, String.class, String.class,String.class,String.class};
	private final List<Alumno> data = new ArrayList<>();
	
	public int add(Alumno a) {data.add(a);int i=data.size()-1;fireTableRowsInserted(i,i); return i;}
	public void updateRow(int row) {data.remove(row);fireTableRowsUpdated(row,row); }
	public void removeAt(int row) {data.remove(row);fireTableRowsDeleted(row,row);}
	public Persona getAt(int row) {return data.get(row);}
	
	

	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return cols.length;
	}
	
	@Override
	public String getColumnName(int col) {
		// TODO Auto-generated method stub
		return cols[col];
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		// TODO Auto-generated method stub
		return types[col];
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		// TODO Auto-generated method stub
		Alumno a = data.get(row);
		return switch (col) {
		case COL_NOMBRE -> a.GetNombre();
		case COL_APELLIDO -> a.GetApellido();
		case COL_DNI -> a.GetDNI();
		case COL_FALTA -> a.GetfAlta();
		case COL_FBAJA -> a.GetfBaja();
		default -> null;
		};
	}

}
