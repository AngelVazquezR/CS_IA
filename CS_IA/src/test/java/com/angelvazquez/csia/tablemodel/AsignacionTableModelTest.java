package com.angelvazquez.csia.tablemodel;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.TableRowSorter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.i18n.Idioma;
import com.angelvazquez.csia.model.Asignacion;

class AsignacionTableModelTest {
    private final Idioma anterior = I18n.getIdioma();
    @AfterEach void restaurarIdioma() { I18n.setIdioma(anterior); }

    private Asignacion asignacion(int id, String fecha) {
        return new Asignacion(id, 10, 20, 1, LocalTime.of(9, 30),
                LocalDate.parse(fecha), LocalDate.parse("2027-06-30"));
    }

    @Test void resuelvePersonasPorIdYTraduceLosDias() {
        var modelo = new AsignacionTableModel();
        modelo.setData(List.of(asignacion(3, "2026-09-01")),
                Map.of(10, "Ruiz, Ana"), Map.of(20, "Pérez, Luis"));
        assertEquals("Ruiz, Ana", modelo.getValueAt(0, 1));
        assertEquals("Pérez, Luis", modelo.getValueAt(0, 2));
        I18n.setIdioma(Idioma.ESPANOL);
        assertEquals("Lunes", modelo.getValueAt(0, 3));
        I18n.setIdioma(Idioma.INGLES);
        assertEquals("Monday", modelo.getValueAt(0, 3));
        assertFalse(modelo.isCellEditable(0, 1));
    }

    @Test void ordenaFechasCronologicamenteYFiltraPorPersona() {
        var modelo = new AsignacionTableModel();
        modelo.setData(List.of(asignacion(1, "2027-01-01"), asignacion(2, "2026-12-31")),
                Map.of(10, "Ruiz, Ana"), Map.of(20, "Pérez, Luis"));
        var sorter = new TableRowSorter<>(modelo);
        sorter.setSortKeys(List.of(new SortKey(5, SortOrder.ASCENDING)));
        assertEquals(1, sorter.convertRowIndexToModel(0));
        sorter.setRowFilter(RowFilter.regexFilter("Ruiz"));
        assertEquals(2, sorter.getViewRowCount());
        sorter.setRowFilter(RowFilter.regexFilter("inexistente"));
        assertEquals(0, sorter.getViewRowCount());
    }

    @Test void recargarSustituyeDatosYConservaIdsSiFaltaUnaPersona() {
        var modelo = new AsignacionTableModel();
        modelo.setData(List.of(asignacion(1, "2026-09-01")), Map.of(), Map.of());
        assertEquals("ID 10", modelo.getValueAt(0, 1));
        assertEquals("ID 20", modelo.getValueAt(0, 2));
        modelo.setData(List.of(), Map.of(), Map.of());
        assertEquals(0, modelo.getRowCount());
    }
    @Test void editarSeleccionOrdenadaNoMutaLosDatosAntesDeGuardar() {
        var modelo = new AsignacionTableModel();
        modelo.setData(List.of(asignacion(1, "2027-01-01"), asignacion(2, "2026-12-31")),
                Map.of(), Map.of());
        var sorter = new TableRowSorter<>(modelo);
        sorter.setSortKeys(List.of(new SortKey(5, SortOrder.ASCENDING)));
        Asignacion copia = modelo.getAt(sorter.convertRowIndexToModel(0));
        assertEquals(2, copia.getId());
        copia.setDiaSemana(5);
        copia.setProfesorId(99);
        copia.setFechaInicio(LocalDate.parse("2026-10-01"));
        Asignacion original = modelo.getAt(1);
        assertEquals(1, original.getDiaSemana());
        assertEquals(10, original.getProfesorId());
        assertEquals(LocalDate.parse("2026-12-31"), original.getFechaInicio());
    }

}
