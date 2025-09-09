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

import Object.ConectionSQL;
import Object.Persona;

import java.sql.ResultSet;
import Object.Profesor;
import Object.ProfesorTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import java.time.*;

public class VisualizarProfesores extends JFrame implements ActionListener{

	JFrame frame;
	static ProfesorTableModel modeloProfesor = new ProfesorTableModel();
	JTable tabla = new JTable(modeloProfesor);
	TableRowSorter<ProfesorTableModel> sorter= new TableRowSorter<>(modeloProfesor);
	
	
	private static final long serialVersionUID = 1L;
	ArrayList<String> dataType = new ArrayList<String>();
	
    JTextField nombreField = new JTextField(10);    
    JTextField apellidoField = new JTextField(10);    
    JTextField dniField = new JTextField(10); 
    JTextField fAltaField = new JTextField(10);   
    JTextField fBajaField = new JTextField(10);
    JButton btnAgregar = new JButton("Agregar");    
    JButton btnModificar = new JButton("Modificar");  
    JButton btnEliminar = new JButton("Eliminar");   

	
	public VisualizarProfesores() {
		frame = new JFrame("Visualizar profesores");
		Profesor a = new Profesor("a","a","a","a","a","a");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 450);
			
	        
	     tabla = new JTable(modeloProfesor);    
	     sorter = new TableRowSorter<>(modeloProfesor);
	     tabla.setRowSorter(sorter);
	     
	     JScrollPane sp = new JScrollPane(tabla);    
	     frame.add(sp);  
	     
	     JTextField filtroField = new JTextField(15);    
	        filtroField.setBounds(30, 10, 200, 30);    
	        frame.add(filtroField, "North");    

	        filtroField.getDocument().addDocumentListener(new DocumentListener() {    
	            public void insertUpdate(DocumentEvent e) { filtro(); }    
	            public void removeUpdate(DocumentEvent e) { filtro(); }    
	            public void changedUpdate(DocumentEvent e) { filtro(); }    

	            public void filtro() {    
	                String texto = filtroField.getText();    
	                if (texto.trim().length() == 0) {    
	                    sorter.setRowFilter(null);    
	                } else {    
	                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));    
	                }    
	            }    
	        });   
	     
	        
	     JPanel panel = new JPanel();  
	        
 

	        panel.add(new JLabel("Nombre:"));    
	        panel.add(nombreField);    
	        panel.add
	        (new JLabel("Apellido:")); 
	        
	        panel.add(apellidoField); 
	        panel.add(new JLabel("DNI:"));
	        panel.add(dniField);
	        
	        panel.add(new JLabel("Fecha de alta:"));    
	        panel.add(fAltaField);    
	        panel.add(new JLabel("Fecha de baja:")); 
	       
	        panel.add(fBajaField);    
	        panel.add(btnAgregar); 
	        panel.add(btnModificar);  
	        panel.add(btnEliminar);    
	        frame.add(panel, "South");  
	        	    	        
	        frame.setSize(1400, 500); 
		    modeloProfesor.add(a);
		     
	        frame.setVisible(true);   

	        ConectionSQL.ProfeFillTable();
	        
	        tabla.getSelectionModel().addListSelectionListener(e -> {
	        	if(e.getValueIsAdjusting()) return;
	        	int viewRow = tabla.getSelectedRow();
	        	if (viewRow >= 0) {
	        		int modelRow = tabla.convertRowIndexToModel(viewRow);
	        		cargarFormulario(modeloProfesor.getAt(modelRow));
	        		btnEliminar.setEnabled(true);
	        	}else {
	        		
	        	}
	        	
	        		
	        	
	        });
	        
	        btnAgregar.addActionListener(new ActionListener() {    
	            public void actionPerformed(ActionEvent e) {    
	                 
	                ConectionSQL.AddProfe(nombreField.getText(), apellidoField.getText(), dniField.getText()
	                		,fAltaField.getText() ,fBajaField.getText() );
	                AddRow(nombreField.getText(), apellidoField.getText(), 
	                		dniField.getText(), fAltaField.getText(), fBajaField.getText());
	            }    
	        });    
	        
	        btnModificar.addActionListener(new ActionListener() {    
	            public void actionPerformed(ActionEvent e) {    
	                 ConectionSQL.ModProfe(nombreField.getText(), apellidoField.getText(),
	                		 dniField.getText(),fAltaField.getText() ,fBajaField.getText());
	                 RecargarVentana();
	                 
	                
	            }    
	        });    
	        
	        btnEliminar.addActionListener(new ActionListener() {    
	            public void actionPerformed(ActionEvent e) {    
	            	int filaSeleccionada = tabla.getSelectedRow();    
	                if (filaSeleccionada >= 0) {    
	                   // modeloProfesor.eliminar(filaSeleccionada);    
	                } else {    
	                    JOptionPane.showMessageDialog(frame, "Por favor selecciona una fila para eliminar.");    
	                }    
	                
	            }    
	        });    
	        
	        
	        
	}

	public static void AddRow(String nombre, String apellido,String dni, String fAlta, String fBaja) {
		System.out.println("AddRow");
		modeloProfesor.add(new Profesor(nombre,apellido,dni,fAlta,fBaja,""));
       //modeloProfesor.add(nuevaFila);   
	}
	
	private void cargarFormulario(Persona persona) {
		nombreField.setText(persona.Nombre);
		apellidoField.setText(persona.Apellido);
		dniField.setText(persona.DNI);
		fAltaField.setText(persona.fAlta);
		fBajaField.setText(persona.fBaja);
	}
	
	public void RecargarVentana() {
	frame.repaint();
	}
	
	public void CerrarVentana() {
		
		Component com = SwingUtilities.getRoot(this);
		 ((Window) com).dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	

}
