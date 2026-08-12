package com.angelvazquez.csia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class ModeloDatosV2Test {
    @Test
    void alumnoV2ExponeDatosEsperados() {
        Alumno alumno = new Alumno(7, "Ana", "Lopez", "12345678A", "ana@example.com");
        assertEquals(7, alumno.getDatabaseId());
        assertEquals("7", alumno.GetID());
        assertEquals("ana@example.com", alumno.getEmail());
    }

    @Test
    void alumnoNuevoNoTieneIdHastaPersistirse() {
        Alumno alumno = new Alumno("Ana", "Lopez", "12345678A", "ana@example.com");
        assertNull(alumno.getDatabaseId());
        assertEquals("", alumno.GetID());
    }

    @Test
    void profesorV2ExponeAsignaturaYEmail() {
        Profesor profesor = new Profesor(3, "Luis", "Perez", "87654321B", "Matematicas", "luis@example.com");
        assertEquals(3, profesor.getDatabaseId());
        assertEquals("Matematicas", profesor.getAsignatura());
        assertEquals("luis@example.com", profesor.getEmail());
    }

    @Test
    void usuarioV2ExponeSusCampos() {
        Usuario usuario = new Usuario(2, "ADMIN", "valor-hash");
        assertEquals(2, usuario.getId());
        assertEquals("ADMIN", usuario.getUsername());
        assertEquals("valor-hash", usuario.getPasswordHash());
    }

    @Test
    void asignacionRepresentaRelacionTemporal() {
        Asignacion asignacion = new Asignacion(4, 8, 1, LocalTime.of(9, 30),
                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
        assertNull(asignacion.getId());
        assertEquals(4, asignacion.getProfesorId());
        assertEquals(8, asignacion.getAlumnoId());
        assertEquals(1, asignacion.getDiaSemana());
    }
}
