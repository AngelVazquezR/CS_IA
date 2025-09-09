package Object.Ventanas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import Object.Alumno;
import Object.ConectionSQL;
import Object.Persona;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class VisualizarAlumnos extends JFrame implements ActionListener{

	JFrame frame1;
	static AlumnoTableModel modelo1 = new AlumnoTableModel();
	JTable tabla = new JTable(modelo1);
	TableRowSorter<AlumnoTableModel> sorter1= new TableRowSorter<>(modelo1);
	
	
	private static final long serialVersionUID = 1L;
	ArrayList<String> dataType1 = new ArrayList<String>();
	

    JTextField nombreField = new JTextField(10);    
    JTextField apellidoField = new JTextField(10);    
    JTextField DNIField = new JTextField(5);   
    JTextField ProfeField = new JTextField(10);
    JButton btnAgregar = new JButton("Agregar");   
    JButton btnModificar = new JButton("Modificar"); 
    JButton btnEliminar = new JButton("Eliminar");   
	
	public VisualizarAlumnos() {
		
		frame1 = new JFrame("Tabla base");
		  
		//Alumno a = new Alumno("a","a","a","","","","a");
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame1.setBounds(100, 100, 650, 450);
		frame1.setVisible(true);
			
	        
	     tabla = new JTable(modelo1);    
	     sorter1 = new TableRowSorter<>(modelo1);
	     tabla.setRowSorter(sorter1);
	     
	     JScrollPane sp = new JScrollPane(tabla);    
	     frame1.add(sp);  
	     
	     JTextField filtroField = new JTextField(15);    
	        filtroField.setBounds(30, 10, 200, 30);    
	        frame1.add(filtroField, "North");    

	        filtroField.getDocument().addDocumentListener(new DocumentListener() {    
	            public void insertUpdate(DocumentEvent e) { filtro(); }    
	            public void removeUpdate(DocumentEvent e) { filtro(); }    
	            public void changedUpdate(DocumentEvent e) { filtro(); }    

	            public void filtro() {    
	                String texto = filtroField.getText();    
	                if (texto.trim().length() == 0) {    
	                    sorter1.setRowFilter(null);    
	                } else {    
	                    sorter1.setRowFilter(RowFilter.regexFilter("(?i)" + texto));    
	                }    
	            }    
	        });   
	        
	        ConectionSQL.AlumFillTable();
	        
	        tabla.getSelectionModel().addListSelectionListener(e -> {
	        	if(e.getValueIsAdjusting()) return;
	        	int viewRow = tabla.getSelectedRow();
	        	if (viewRow >= 0) {
	        		int modelRow = tabla.convertRowIndexToModel(viewRow);
	        		cargarFormulario(modelo1.getAt(modelRow));
	        		btnEliminar.setEnabled(true);
	        	}else {
	        		
	        	}		        	
	        });
	     
	        
	     JPanel panel = new JPanel();  

	        panel.add(new JLabel("Nombre:"));    
	        panel.add(nombreField);    
	        panel.add(new JLabel("Apellido:")); 
	      	panel.add(apellidoField);    
	        panel.add(new JLabel("DNI:"));    
	        panel.add(DNIField);     
	        panel.add(new JLabel("Profesor Asignado:"));    
	        panel.add(ProfeField); 
	        panel.add(btnAgregar);    
	        panel.add(btnModificar);  
	        panel.add(btnEliminar);    
	        frame1.add(panel, "South");  
	        
	        frame1.setSize(1100, 500); 
		    //modelo.add(a);
		     
	         

	        
	}

	public static void AddRow(String nombre, String apellido,String dni,String prof) {
		System.out.println("AddRow");
		modelo1.add(new Alumno(nombre,apellido,dni,"","","",prof));
	}
	
	private void cargarFormulario(Persona persona) {
		nombreField.setText(persona.Nombre);
		apellidoField.setText(persona.Apellido);
		DNIField.setText(persona.DNI);
	}	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	

}
