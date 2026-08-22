package com.angelvazquez.csia.tablemodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.angelvazquez.csia.model.Alumno;
import com.angelvazquez.csia.model.Profesor;

class TableModelV2Test {

    @Test
    void alumnoTableModelMuestraIdNombreApellidoDniYEmail() {
        AlumnoTableModel model = new AlumnoTableModel();
        Alumno alumno = new Alumno(7, "Ana", "Lopez", "12345678A", "ana@example.com");

        model.add(alumno);

        assertEquals(5, model.getColumnCount());
        assertEquals("ID", model.getColumnName(0));
        assertEquals("Email", model.getColumnName(4));
        assertEquals(7, model.getValueAt(0, AlumnoTableModel.COL_ID));
        assertEquals("Ana", model.getValueAt(0, AlumnoTableModel.COL_NOMBRE));
        assertEquals("ana@example.com", model.getValueAt(0, AlumnoTableModel.COL_EMAIL));
        assertSame(alumno, model.getAt(0));
    }

    @Test
    void alumnoTableModelSetDataSustituyeElContenidoAnterior() {
        AlumnoTableModel model = new AlumnoTableModel();
        model.add(new Alumno(1, "Uno", "A", "1A", "uno@example.com"));

        model.setData(List.of(
                new Alumno(2, "Dos", "B", "2B", "dos@example.com"),
                new Alumno(3, "Tres", "C", "3C", "tres@example.com")));

        assertEquals(2, model.getRowCount());
        assertEquals(2, model.getValueAt(0, AlumnoTableModel.COL_ID));
        assertEquals(3, model.getValueAt(1, AlumnoTableModel.COL_ID));
    }

    @Test
    void profesorTableModelMuestraAsignaturaYEmailEnLugarDeFechas() {
        ProfesorTableModel model = new ProfesorTableModel();
        Profesor profesor = new Profesor(11, "Luis", "Perez", "87654321B",
                "Matematicas", "luis@example.com");

        model.add(profesor);

        assertEquals(6, model.getColumnCount());
        assertEquals("Asignatura", model.getColumnName(ProfesorTableModel.COL_ASIGNATURA));
        assertEquals("Email", model.getColumnName(ProfesorTableModel.COL_EMAIL));
        assertEquals("Matematicas", model.getValueAt(0, ProfesorTableModel.COL_ASIGNATURA));
        assertEquals("luis@example.com", model.getValueAt(0, ProfesorTableModel.COL_EMAIL));
        assertSame(profesor, model.getAt(0));
    }

    @Test
    void updateRowSustituyeLaEntidadSinEliminarLaFila() {
        ProfesorTableModel model = new ProfesorTableModel();
        model.add(new Profesor(1, "Luis", "Perez", "1B", "Fisica", "a@example.com"));

        Profesor actualizado = new Profesor(1, "Luis", "Perez", "1B",
                "Quimica", "b@example.com");
        model.updateRow(0, actualizado);

        assertEquals(1, model.getRowCount());
        assertSame(actualizado, model.getAt(0));
        assertEquals("Quimica", model.getValueAt(0, ProfesorTableModel.COL_ASIGNATURA));
    }
}
