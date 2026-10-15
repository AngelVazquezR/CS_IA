package com.angelvazquez.csia.model;

/**
 * Datos comunes de alumnos y profesores.
 *
 * Mantiene temporalmente la API del modelo legado para no romper la UI
 * mientras se completa la migracion al modelo de datos v2.
 */
public class Persona {

    private String Nombre;
    private String Apellido;
    private String ID;
    private Integer databaseId;
    private String DNI;
    private String email;
    private String fAlta;
    private String fBaja;

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

    public String getNombre() {
        return Nombre;
    }

    public String getApellido() {
        return Apellido;
    }

    /**
     * API legada. Para entidades v2 devuelve el identificador numerico como texto.
     */
    public String getId() {
        return ID;
    }

    public Integer getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Integer databaseId) {
        this.databaseId = databaseId;
        this.ID = databaseId == null ? "" : databaseId.toString();
    }

    public String getDni() {
        return DNI;
    }

    public void setDni(String dni) {
        this.DNI = dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFAlta() {
        return fAlta;
    }

    public String getFBaja() {
        return fBaja;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public void setApellido(String apellido) {
        Apellido = apellido;
    }

    public void setFAlta(String falta) {
        fAlta = falta;
    }

    public void setFBaja(String fbaja) {
        fBaja = fbaja;
    }
}
