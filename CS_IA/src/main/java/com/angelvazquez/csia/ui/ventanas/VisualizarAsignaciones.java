package com.angelvazquez.csia.ui.ventanas;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
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
    private final JTable tabla = new JTable(modelo);
    private final JScrollPane scroll = new JScrollPane(tabla);
    private final int[] anchosContenido = new int[modelo.getColumnCount()];
    private final JButton actualizar = new JButton(I18n.get("assignments.refresh"));
    private final JButton agregar = new JButton(I18n.get("table.add"));
    private final JButton modificar = new JButton(I18n.get("table.edit"));
    private final JButton eliminar = new JButton(I18n.get("table.delete"));
    private final JTextField[] detalle = new JTextField[modelo.getColumnCount()];
    private boolean cargando;
    private final JLabel estado = new JLabel();
    private final ConfigDB configuracion;

    public VisualizarAsignaciones(Window parent) {
        super(parent);
        configuracion = Main.getConfiguracion();
        setTitle(I18n.get("home.assignments"));
        setBounds(100, 100, 1100, 500);
        setLayout(new BorderLayout());
        // Repartir explícitamente el espacio sobrante solo entre Tutor y Alumno.
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int col = 4; col <= 6; col++) {
            tabla.getColumnModel().getColumn(col).setCellRenderer(centrado);
        }
        DefaultTableCellRenderer izquierda = new DefaultTableCellRenderer();
        izquierda.setHorizontalAlignment(SwingConstants.LEFT);
        tabla.getColumnModel().getColumn(3).setCellRenderer(izquierda);
        TableRowSorter<AsignacionTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
        ajustarAnchos();
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { repartirEspacioLibre(); }
        });
        add(scroll, BorderLayout.CENTER);
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
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> mostrarSeleccion());
        agregar.addActionListener(e -> abrirFormulario(null));
        modificar.addActionListener(e -> {
            Asignacion a = seleccionada();
            if (a != null) abrirFormulario(a);
        });
        eliminar.addActionListener(e -> eliminarSeleccionada());
        JPanel campos = new JPanel(new GridLayout(2, modelo.getColumnCount(), 8, 4));
        for (int col = 0; col < detalle.length; col++) {
            detalle[col] = new JTextField();
            detalle[col].setEditable(false);
            JLabel etiquetaCampo = new JLabel(modelo.getColumnName(col));
            etiquetaCampo.setLabelFor(detalle[col]);
            campos.add(etiquetaCampo);
        }
        for (JTextField campo : detalle) campos.add(campo);
        JPanel acciones = new JPanel();
        acciones.add(estado); acciones.add(agregar); acciones.add(modificar);
        acciones.add(eliminar); acciones.add(actualizar); acciones.add(atras);
        JPanel inferior = new JPanel(new BorderLayout(8, 8));
        inferior.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        inferior.add(campos, BorderLayout.CENTER);
        inferior.add(acciones, BorderLayout.SOUTH);
        add(inferior, BorderLayout.SOUTH);
        mostrarSeleccion();
        recargarDatos();
    }

    private Asignacion seleccionada() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : modelo.getAt(tabla.convertRowIndexToModel(fila));
    }

    private void mostrarSeleccion() {
        int fila = tabla.getSelectedRow();
        int filaModelo = fila < 0 ? -1 : tabla.convertRowIndexToModel(fila);
        for (int col = 0; col < detalle.length; col++) {
            if (detalle[col] != null) {
                detalle[col].setText(filaModelo < 0 ? "" : String.valueOf(modelo.getValueAt(filaModelo, col)));
                detalle[col].setCaretPosition(0);
            }
        }
        modificar.setEnabled(!cargando && fila >= 0);
        eliminar.setEnabled(!cargando && fila >= 0);
    }

    private void abrirFormulario(Asignacion a) {
        AsignarTab formulario = new AsignarTab(this, a, this::recargarDatos);
        setVisible(false);
        formulario.setVisible(true);
    }

    private void eliminarSeleccionada() {
        Asignacion a = seleccionada();
        if (a == null) return;
        if (JOptionPane.showConfirmDialog(this, I18n.get("assignments.confirmDelete", a.getId()),
                I18n.get("table.confirmDelete"), JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            if (!new AsignacionRepository(new DatabaseConnectionFactory(), configuracion).eliminar(a.getId())) {
                JOptionPane.showMessageDialog(this, I18n.get("assignments.notFound"),
                        I18n.get("app.error"), JOptionPane.ERROR_MESSAGE);
            }
            recargarDatos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, I18n.get("assignments.deleteError", e.getMessage()),
                    I18n.get("app.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private record Datos(List<Asignacion> asignaciones, Map<Integer, String> profesores,
            Map<Integer, String> alumnos) { }

    private void recargarDatos() {
        cargando = true;
        agregar.setEnabled(false);
        mostrarSeleccion();
        actualizar.setEnabled(false);
        estado.setText(I18n.get("assignments.loading"));
        new SwingWorker<Datos, Void>() {
            @Override protected Datos doInBackground() throws Exception {
                DatabaseConnectionFactory factory = new DatabaseConnectionFactory();
                Map<Integer, String> profesores = new HashMap<>();
                for (var p : new ProfesorRepository(factory, configuracion).listar()) {
                    profesores.put(p.getDatabaseId(), p.GetApellido() + ", " + p.GetNombre());
                }
                Map<Integer, String> alumnos = new HashMap<>();
                for (var a : new AlumnoRepository(factory, configuracion).listar()) {
                    alumnos.put(a.getDatabaseId(), a.GetApellido() + ", " + a.GetNombre());
                }
                return new Datos(new AsignacionRepository(factory, configuracion).listar(), profesores, alumnos);
            }
            @Override protected void done() {
                if (!isDisplayable()) return;
                try {
                    Datos datos = get();
                    modelo.setData(datos.asignaciones(), datos.profesores(), datos.alumnos());
                    ajustarAnchos();
                    estado.setText(datos.asignaciones().isEmpty() ? I18n.get("assignments.empty") : "");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError(e);
                } catch (ExecutionException e) {
                    mostrarError(e.getCause());
                } finally {
                    cargando = false;
                    agregar.setEnabled(true);
                    actualizar.setEnabled(true);
                    mostrarSeleccion();
                }
            }
        }.execute();
    }

    private void ajustarAnchos() {
        for (int col = 0; col < tabla.getColumnCount(); col++) {
            TableColumn columna = tabla.getColumnModel().getColumn(col);
            int indiceModelo = columna.getModelIndex();
            TableCellRenderer cabecera = columna.getHeaderRenderer();
            if (cabecera == null) cabecera = tabla.getTableHeader().getDefaultRenderer();
            int ancho = cabecera.getTableCellRendererComponent(tabla, columna.getHeaderValue(),
                    false, false, -1, col).getPreferredSize().width;
            TableCellRenderer celda = columna.getCellRenderer();
            if (celda == null) celda = tabla.getDefaultRenderer(modelo.getColumnClass(indiceModelo));
            // Medir todas las filas, incluso si hay un filtro activo al actualizar.
            for (int fila = 0; fila < modelo.getRowCount(); fila++) {
                int anchoCelda = celda.getTableCellRendererComponent(tabla,
                        modelo.getValueAt(fila, indiceModelo), false, false, -1, col)
                        .getPreferredSize().width;
                ancho = Math.max(ancho, anchoCelda);
            }
            // Margen para el texto y el indicador de ordenación de la cabecera.
            ancho += 24;
            anchosContenido[indiceModelo] = Math.max(ancho, columna.getMinWidth());
        }
        repartirEspacioLibre();
    }

    private void repartirEspacioLibre() {
        int anchoTotal = 0;
        for (int ancho : anchosContenido) anchoTotal += ancho;
        int sobrante = Math.max(0, scroll.getViewport().getExtentSize().width - anchoTotal);
        for (int col = 0; col < tabla.getColumnCount(); col++) {
            TableColumn columna = tabla.getColumnModel().getColumn(col);
            int indiceModelo = columna.getModelIndex();
            int ancho = anchosContenido[indiceModelo];
            if (indiceModelo == 1) ancho += sobrante / 2;
            else if (indiceModelo == 2) ancho += sobrante - sobrante / 2;
            columna.setPreferredWidth(ancho);
            columna.setWidth(ancho);
        }
    }

    private void mostrarError(Throwable error) {
        estado.setText(I18n.get("assignments.failed"));
        JOptionPane.showMessageDialog(this, I18n.get("assignments.loadError", error.getMessage()),
                I18n.get("app.error"), JOptionPane.ERROR_MESSAGE);
    }
}
