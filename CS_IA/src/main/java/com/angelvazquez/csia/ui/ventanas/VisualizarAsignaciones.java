package com.angelvazquez.csia.ui.ventanas;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import com.angelvazquez.csia.Main;
import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.repository.AlumnoRepository;
import com.angelvazquez.csia.database.repository.AsignacionRepository;
import com.angelvazquez.csia.database.repository.ProfesorRepository;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.model.Asignacion;
import com.angelvazquez.csia.tablemodel.AsignacionTableModel;

/** Consulta de asignaciones con búsqueda y navegación de vuelta a Inicio. */
public class VisualizarAsignaciones extends VentanaSecundaria {
    private static final long serialVersionUID = 1L;
    private final AsignacionTableModel modelo = new AsignacionTableModel();
    private final JButton actualizar = new JButton(I18n.get("assignments.refresh"));
    private final JLabel estado = new JLabel();
    private final ConfigDB configuracion;

    public VisualizarAsignaciones(Window parent) {
        super(parent);
        configuracion = Main.getConfiguracion();
        setTitle(I18n.get("home.assignments"));
        setBounds(100, 100, 1100, 500);
        setLayout(new BorderLayout());
        JTable tabla = new JTable(modelo);
        TableRowSorter<AsignacionTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JTextField filtro = new JTextField(25);
        JLabel etiqueta = new JLabel(I18n.get("assignments.search"));
        etiqueta.setLabelFor(filtro);
        JPanel busqueda = new JPanel();
        busqueda.add(etiqueta); busqueda.add(filtro);
        add(busqueda, BorderLayout.NORTH);
        filtro.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = filtro.getText();
                sorter.setRowFilter(texto.isBlank() ? null
                        : RowFilter.regexFilter("(?iu)" + Pattern.quote(texto)));
            }
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        JButton atras = new JButton(I18n.get("app.back"));
        atras.addActionListener(e -> volverAlPadre());
        actualizar.addActionListener(e -> recargarDatos());
        JPanel acciones = new JPanel();
        acciones.add(estado); acciones.add(actualizar); acciones.add(atras);
        add(acciones, BorderLayout.SOUTH);
        recargarDatos();
    }

    private record Datos(List<Asignacion> asignaciones, Map<Integer, String> profesores,
            Map<Integer, String> alumnos) { }

    private void recargarDatos() {
        actualizar.setEnabled(false);
        estado.setText(I18n.get("assignments.loading"));
        new SwingWorker<Datos, Void>() {
            @Override protected Datos doInBackground() throws Exception {
                DatabaseConnectionFactory factory = new DatabaseConnectionFactory();
                Map<Integer, String> profesores = new HashMap<>();
                for (var p : new ProfesorRepository(factory, configuracion).listar()) {
                    profesores.put(p.getDatabaseId(), p.GetNombre() + " " + p.GetApellido() + " (" + p.GetDNI() + ")");
                }
                Map<Integer, String> alumnos = new HashMap<>();
                for (var a : new AlumnoRepository(factory, configuracion).listar()) {
                    alumnos.put(a.getDatabaseId(), a.GetNombre() + " " + a.GetApellido() + " (" + a.GetDNI() + ")");
                }
                return new Datos(new AsignacionRepository(factory, configuracion).listar(), profesores, alumnos);
            }
            @Override protected void done() {
                if (!isDisplayable()) return;
                try {
                    Datos datos = get();
                    modelo.setData(datos.asignaciones(), datos.profesores(), datos.alumnos());
                    estado.setText(datos.asignaciones().isEmpty() ? I18n.get("assignments.empty") : "");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError(e);
                } catch (ExecutionException e) {
                    mostrarError(e.getCause());
                } finally {
                    actualizar.setEnabled(true);
                }
            }
        }.execute();
    }

    private void mostrarError(Throwable error) {
        estado.setText(I18n.get("assignments.failed"));
        JOptionPane.showMessageDialog(this, I18n.get("assignments.loadError", error.getMessage()),
                I18n.get("app.error"), JOptionPane.ERROR_MESSAGE);
    }
}
