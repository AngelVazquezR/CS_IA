package com.angelvazquez.csia.model;

import java.time.LocalTime;

public class Profesor extends Persona {

    /** Campos legados; se mantienen hasta migrar por completo la UI. */
    @Deprecated
    public LocalTime F_Alta = LocalTime.now();
    @Deprecated
    public LocalTime F_Baja = LocalTime.now();

    private String asignatura;

    /** Constructor legado. */
    public Profesor(String nombre, String apellido, String dni, String falta,
            String fbaja, String id) {
        super(nombre, apellido, dni, falta, fbaja, id);
        asignatura = "";
    }

    /** Constructor del modelo v2. */
    public Profesor(Integer id, String nombre, String apellido, String dni,
            String asignatura, String email) {
        super(id, nombre, apellido, dni, email);
        this.asignatura = asignatura;
    }

    /** Constructor para nuevas altas antes de que SQLite genere el ID. */
    public Profesor(String nombre, String apellido, String dni,
            String asignatura, String email) {
        this(null, nombre, apellido, dni, asignatura, email);
    }

    public String getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(String asignatura) {
        this.asignatura = asignatura;
    }

    @Deprecated
    public LocalTime GetF_Alta() {
        return F_Alta;
    }

    @Deprecated
    public LocalTime GetF_Baja() {
        return F_Baja;
    }

    @Deprecated
    public void SetF_Alta(LocalTime fAlta) {
        F_Alta = fAlta;
    }

    @Deprecated
    public void SetF_Baja(LocalTime fBaja) {
        F_Baja = fBaja;
    }
}
