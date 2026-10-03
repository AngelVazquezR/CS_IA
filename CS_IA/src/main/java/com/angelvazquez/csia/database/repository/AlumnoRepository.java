package com.angelvazquez.csia.database.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.model.Alumno;

/** Persistencia de alumnos sobre la tabla STUDENTS. */
public final class AlumnoRepository {

    private static final String FIND_ALL = """
            SELECT STUDENT_ID, FIRST_NAME, LAST_NAME, DNI, EMAIL
            FROM STUDENTS
            ORDER BY STUDENT_ID
            """;

    private static final String FIND_BY_DNI = """
            SELECT STUDENT_ID, FIRST_NAME, LAST_NAME, DNI, EMAIL
            FROM STUDENTS
            WHERE DNI = ?
            """;

    private static final String INSERT = """
            INSERT INTO STUDENTS (FIRST_NAME, LAST_NAME, DNI, EMAIL)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE STUDENTS
            SET FIRST_NAME = ?, LAST_NAME = ?, DNI = ?, EMAIL = ?
            WHERE STUDENT_ID = ?
            """;

    private static final String DELETE = "DELETE FROM STUDENTS WHERE STUDENT_ID = ?";

    /**
     * Cada operación SQL abre y cierra su propia conexión mediante try-with-resources.
     * Las comprobaciones de DNI y las escrituras se realizan en llamadas separadas.
     */
    private final DatabaseConnectionFactory connectionFactory;
    private final ConfigDB configuration;

    public AlumnoRepository(DatabaseConnectionFactory connectionFactory, ConfigDB configuration) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
        this.configuration = Objects.requireNonNull(configuration);
    }

    /**
     * Obtiene todas las filas ordenadas por STUDENT_ID.
     *
     * @return lista de entidades nuevas, vacía si no hay registros; modificarla no persiste cambios
     * @throws SQLException si falla la conexión o la consulta
     */
    public List<Alumno> listar() throws SQLException {
        List<Alumno> alumnos = new ArrayList<>();
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                alumnos.add(map(rows));
            }
        }
        return alumnos;
    }

    /**
     * Busca el primer registro que coincide con el DNI enviado a la consulta.
     *
     * @param dni valor que se compara sin recortar ni cambiar mayúsculas en este repositorio
     * @return entidad encontrada, o un Optional vacío si no hay coincidencias
     * @throws SQLException si falla la conexión o la consulta
     */
    public Optional<Alumno> buscarPorDni(String dni) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_BY_DNI)) {
            statement.setString(1, dni);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        }
    }

    /**
     * Inserta una nueva entidad y asigna a la instancia recibida el ID generado.
     * No realiza una comprobación previa de DNI duplicado ni valida formatos de los campos.
     *
     * @param alumno entidad no nula cuyos datos se van a guardar
     * @return STUDENT_ID generado, también guardado en la entidad
     * @throws NullPointerException si la entidad es nula
     * @throws SQLException si falla la inserción o no se obtiene la clave generada
     */
    public int agregar(Alumno alumno) throws SQLException {
        Objects.requireNonNull(alumno);
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, alumno.GetNombre());
            statement.setString(2, alumno.GetApellido());
            statement.setString(3, alumno.GetDNI());
            statement.setString(4, alumno.getEmail());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se ha obtenido STUDENT_ID al crear el alumno.");
                }
                int id = keys.getInt(1);
                alumno.setDatabaseId(id);
                return id;
            }
        }
    }

    /**
     * Actualiza los campos del registro identificado por el ID de la entidad.
     * No realiza una comprobación previa de DNI duplicado ni valida formatos de los campos.
     *
     * @param alumno entidad no nula con identificador de base de datos
     * @return true si JDBC informa de exactamente una fila afectada; false en otro caso
     * @throws NullPointerException si la entidad es nula
     * @throws IllegalArgumentException si falta el identificador
     * @throws SQLException si falla la actualización
     */
    public boolean modificar(Alumno alumno) throws SQLException {
        Objects.requireNonNull(alumno);
        if (alumno.getDatabaseId() == null) {
            throw new IllegalArgumentException("El alumno debe tener STUDENT_ID para modificarse.");
        }
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, alumno.GetNombre());
            statement.setString(2, alumno.GetApellido());
            statement.setString(3, alumno.GetDNI());
            statement.setString(4, alumno.getEmail());
            statement.setInt(5, alumno.getDatabaseId());
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Elimina el registro indicado; las restricciones de la base de datos pueden impedirlo.
     *
     * @param studentId identificador del registro que se desea eliminar
     * @return true si JDBC informa de exactamente una fila afectada; false en otro caso
     * @throws SQLException si falla el borrado, incluida una restricción referencial
     */
    public boolean eliminar(int studentId) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, studentId);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Consulta si existe algún registro con el DNI, usando la misma búsqueda de buscarPorDni.
     * El resultado no reserva el DNI ni garantiza su unicidad en una escritura posterior.
     *
     * @param dni valor que se compara sin normalización en este repositorio
     * @return true si la búsqueda encuentra una fila
     * @throws SQLException si falla la consulta
     */
    public boolean existeDni(String dni) throws SQLException {
        return buscarPorDni(dni).isPresent();
    }

    private Alumno map(ResultSet rows) throws SQLException {
        return new Alumno(
                rows.getInt("STUDENT_ID"),
                rows.getString("FIRST_NAME"),
                rows.getString("LAST_NAME"),
                rows.getString("DNI"),
                rows.getString("EMAIL")
        );
    }
}
