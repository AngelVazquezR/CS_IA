package com.angelvazquez.csia.model;

/**
 * Datos comunes de alumnos y profesores.
 *
 * Mantiene temporalmente la API del modelo legado para no romper la UI
 * mientras se completa la migracion al modelo de datos v2.
 */
public class Persona {

    public String Nombre;
    public String Apellido;
    private String ID;
    private Integer databaseId;
    public String DNI;
    private String email;
    public String fAlta;
    public String fBaja;

    /** Constructor legado. */
    public Persona(String nombre, String apellido, String dni, String falta,
            String fbaja, String id) {
        Nombre = nombre;
        Apellido = apellido;
        ID = id;
        DNI = dni;
        fAlta = falta;
        fBaja = fbaja;
        email = "";
    }

    /** Constructor del modelo v2. */
    public Persona(Integer id, String nombre, String apellido, String dni,
            String email) {
        this(nombre, apellido, dni, "", "",
                id == null ? "" : id.toString());
        this.databaseId = id;
        this.email = email;
    }

    public String GetNombre() {
        return Nombre;
    }

    public String GetApellido() {
        return Apellido;
    }

    /**
     * API legada. Para entidades v2 devuelve el identificador numerico como texto.
     */
    public String GetID() {
        return ID;
    }

    public Integer getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Integer databaseId) {
        this.databaseId = databaseId;
        this.ID = databaseId == null ? "" : databaseId.toString();
    }

    public String GetDNI() {
        return DNI;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String GetfAlta() {
        return fAlta;
    }

    public String GetfBaja() {
        return fBaja;
    }

    public void SetNombre(String nombre) {
        Nombre = nombre;
    }

    public void SetApellido(String apellido) {
        Apellido = apellido;
    }

    public void SetfAlta(String falta) {
        fAlta = falta;
    }

    public void SetfBaja(String fbaja) {
        fBaja = fbaja;
    }
}
