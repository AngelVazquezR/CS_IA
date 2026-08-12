package com.angelvazquez.csia.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Alumno extends Persona {

    /** Campo legado; la relacion profesor-alumno pasa a ASSIGNMENTS en v2. */
    @Deprecated
    public String profAsing;

    /** Constructor legado. */
    public Alumno(String nombre, String apellido, String dni, String fAlta,
            String fBaja, String id, String profasing) {
        super(nombre, apellido, dni, fAlta, fBaja, id);
        profAsing = profasing;
    }

    /** Constructor del modelo v2. */
    public Alumno(Integer id, String nombre, String apellido, String dni,
            String email) {
        super(id, nombre, apellido, dni, email);
        profAsing = "";
    }

    /** Constructor para nuevas altas antes de que SQLite genere el ID. */
    public Alumno(String nombre, String apellido, String dni, String email) {
        this(null, nombre, apellido, dni, email);
    }

    @Deprecated
    public String GetProf() {
        return profAsing;
    }

    @Deprecated
    public void SetProf(String profAsing) {
        this.profAsing = profAsing;
    }

    /** Compatibilidad con la carga legada desde ResultSet. */
    public void CargaDatos(ResultSet rs) {
        try {
            super.SetNombre(rs.getString("NOMBRE"));
        } catch (SQLException e) {
            throw new IllegalStateException("No se han podido cargar los datos del alumno.", e);
        }
    }
}
