package com.angelvazquez.csia.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Relacion temporal entre un profesor y un alumno.
 * Representa una fila de la tabla ASSIGNMENTS del modelo de datos v2.
 */
public class Asignacion {

    private Integer id;
    private Integer profesorId;
    private Integer alumnoId;
    private int diaSemana;
    private LocalTime horaInicio;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public Asignacion(Integer id, Integer profesorId, Integer alumnoId, int diaSemana,
            LocalTime horaInicio, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.profesorId = profesorId;
        this.alumnoId = alumnoId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Asignacion(Integer profesorId, Integer alumnoId, int diaSemana,
            LocalTime horaInicio, LocalDate fechaInicio, LocalDate fechaFin) {
        this(null, profesorId, alumnoId, diaSemana, horaInicio, fechaInicio, fechaFin);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProfesorId() {
        return profesorId;
    }

    public void setProfesorId(Integer profesorId) {
        this.profesorId = profesorId;
    }

    public Integer getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Integer alumnoId) {
        this.alumnoId = alumnoId;
    }

    public int getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(int diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
}
