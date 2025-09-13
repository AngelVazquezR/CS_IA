package Object;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;


public class AlumnoTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	
	public static final int COL_NOMRE=0;
	public static final int COL_APELLIDO=1;
	public static final int COL_DNI=2;
	public static final int COL_PROFESOR_ASIGNADO=3;

	
	private String[] cols1 = new String[]{"Nombre","Apellido","DNI","Profesor asignado"};
	private Class<?>[] types1 = new Class<?>[]{String.class, String.class, String.class, String.class};
	private List<Alumno> data1 = new ArrayList<>();
	
	
	public int add(Alumno a) {data1.add(a);int i=data1.size()-1;fireTableRowsInserted(i,i); return i;}
	public void updateRow(int row) {data1.remove(row);fireTableRowsUpdated(row,row); }
	public void removeAt(int row) {data1.remove(row);fireTableRowsDeleted(row,row);}
	public Persona getAt(int row) {return data1.get(row);}
	
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return data1.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return cols1.length;
	}
	
	@Override
	public String getColumnName(int col) {
		// TODO Auto-generated method stub
		return cols1[col];
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		// TODO Auto-generated method stub
		return types1[col];
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		// TODO Auto-generated method stub
		Alumno a = data1.get(row);
		return switch (col) {
		case COL_NOMRE -> a.Nombre;
		case COL_APELLIDO -> a.Apellido;
		case COL_DNI -> a.DNI;
		case COL_PROFESOR_ASIGNADO -> a.profAsing;
		default -> null;
		};
	}

}
