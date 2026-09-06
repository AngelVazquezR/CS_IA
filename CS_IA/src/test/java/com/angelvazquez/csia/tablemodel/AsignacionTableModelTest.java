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
                Map.of(10, "Ana Ruiz (123)"), Map.of(20, "Luis Pérez (456)"));
        assertEquals("Ana Ruiz (123)", modelo.getValueAt(0, 1));
        assertEquals("Luis Pérez (456)", modelo.getValueAt(0, 2));
        I18n.setIdioma(Idioma.ESPANOL);
        assertEquals("Lunes", modelo.getValueAt(0, 3));
        I18n.setIdioma(Idioma.INGLES);
        assertEquals("Monday", modelo.getValueAt(0, 3));
        assertFalse(modelo.isCellEditable(0, 1));
    }

    @Test void ordenaFechasCronologicamenteYFiltraPorPersona() {
        var modelo = new AsignacionTableModel();
        modelo.setData(List.of(asignacion(1, "2027-01-01"), asignacion(2, "2026-12-31")),
                Map.of(10, "Ana Ruiz (123)"), Map.of(20, "Luis Pérez (456)"));
        var sorter = new TableRowSorter<>(modelo);
        sorter.setSortKeys(List.of(new SortKey(5, SortOrder.ASCENDING)));
        assertEquals(1, sorter.convertRowIndexToModel(0));
        sorter.setRowFilter(RowFilter.regexFilter("123"));
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
}
