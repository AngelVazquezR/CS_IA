package com.angelvazquez.csia.database.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.model.Asignacion;

/** Persistencia de relaciones profesor-alumno sobre ASSIGNMENTS. */
public final class AsignacionRepository {

    private static final String FIND_ALL = """
            SELECT ASSIGNMENT_ID, TEACHER_ID, STUDENT_ID, DAY_OF_WEEK,
                   START_TIME, START_DATE, END_DATE
            FROM ASSIGNMENTS
            ORDER BY ASSIGNMENT_ID
            """;

    private static final String INSERT = """
            INSERT INTO ASSIGNMENTS
                (TEACHER_ID, STUDENT_ID, DAY_OF_WEEK, START_TIME, START_DATE, END_DATE)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE ASSIGNMENTS
            SET TEACHER_ID = ?, STUDENT_ID = ?, DAY_OF_WEEK = ?,
                START_TIME = ?, START_DATE = ?, END_DATE = ?
            WHERE ASSIGNMENT_ID = ?
            """;

    private static final String DELETE = "DELETE FROM ASSIGNMENTS WHERE ASSIGNMENT_ID = ?";

    /**
     * Cada operación SQL abre y cierra su propia conexión mediante try-with-resources.
     */
    private final DatabaseConnectionFactory connectionFactory;
    private final ConfigDB configuration;

    public AsignacionRepository(DatabaseConnectionFactory connectionFactory, ConfigDB configuration) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
        this.configuration = Objects.requireNonNull(configuration);
    }

    /**
     * Lee todas las asignaciones por ASSIGNMENT_ID y convierte las fechas y horas almacenadas.
     *
     * @return lista de entidades nuevas, vacía si no hay registros
     * @throws SQLException si falla la conexión o la consulta
     * @throws java.time.format.DateTimeParseException si una fecha u hora almacenada no se puede interpretar
     */
    public List<Asignacion> listar() throws SQLException {
        List<Asignacion> asignaciones = new ArrayList<>();
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                asignaciones.add(map(rows));
            }
        }
        return asignaciones;
    }

    /**
     * Valida e inserta la asignación y actualiza su ID con la clave generada.
     * La validación comprueba datos obligatorios, día entre 1 y 7 y fecha final no anterior
     * a la inicial; la base comprueba referencias y rechaza profesor y alumno con el mismo DNI.
     *
     * @param asignacion entidad con IDs de profesor y alumno, hora y fechas no nulos
     * @return ASSIGNMENT_ID generado, también guardado en la entidad
     * @throws NullPointerException si la entidad o alguno de esos datos son nulos
     * @throws IllegalArgumentException si el día o el orden de fechas no son válidos,
     *         o profesor y alumno tienen el mismo DNI normalizado
     * @throws SQLException si falla la inserción o no se obtiene la clave generada
     */
    public int agregar(Asignacion asignacion) throws SQLException {
        Objects.requireNonNull(asignacion);
        validate(asignacion);
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, asignacion, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se ha obtenido ASSIGNMENT_ID al crear la asignacion.");
                }
                int id = keys.getInt(1);
                asignacion.setId(id);
                return id;
            }
        } catch (SQLException e) {
            throw RepositoryErrors.traducir(e);
        }
    }

    /**
     * Valida y actualiza la asignación identificada por su ID, con las mismas reglas de agregar.
     * No comprueba solapamientos de horarios.
     *
     * @param asignacion entidad con ID de asignación y datos obligatorios completos
     * @return true si JDBC informa de exactamente una fila afectada; false en otro caso
     * @throws NullPointerException si la entidad o un dato obligatorio son nulos
     * @throws IllegalArgumentException si falta el ID, el día o las fechas no son válidos,
     *         o profesor y alumno tienen el mismo DNI normalizado
     * @throws SQLException si falla la actualización
     */
    public boolean modificar(Asignacion asignacion) throws SQLException {
        Objects.requireNonNull(asignacion);
        validate(asignacion);
        if (asignacion.getId() == null) {
            throw new IllegalArgumentException("La asignacion debe tener ASSIGNMENT_ID para modificarse.");
        }
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            bind(statement, asignacion, true);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw RepositoryErrors.traducir(e);
        }
    }

    /**
     * Elimina la asignación identificada sin modificar la entidad que pueda tener el llamador.
     *
     * @param assignmentId identificador de la asignación que se desea eliminar
     * @return true si JDBC informa de exactamente una fila afectada; false en otro caso
     * @throws SQLException si falla el borrado
     */
    public boolean eliminar(int assignmentId) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, assignmentId);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Guarda hora y fechas con el formato ISO de LocalTime y LocalDate para su lectura con parse.
     * includeId añade el parámetro del WHERE de UPDATE; INSERT deja que la base genere el ID.
     */
    private void bind(PreparedStatement statement, Asignacion asignacion, boolean includeId)
            throws SQLException {
        statement.setInt(1, asignacion.getProfesorId());
        statement.setInt(2, asignacion.getAlumnoId());
        statement.setInt(3, asignacion.getDiaSemana());
        statement.setString(4, asignacion.getHoraInicio().toString());
        statement.setString(5, asignacion.getFechaInicio().toString());
        statement.setString(6, asignacion.getFechaFin().toString());
        if (includeId) {
            statement.setInt(7, asignacion.getId());
        }
    }

    /**
     * Valida campos obligatorios y coherencia temporal antes de escribir.
     * Las referencias y autoasignaciones se comprueban en la base; no detecta solapamientos.
     */
    private void validate(Asignacion asignacion) {
        Objects.requireNonNull(asignacion.getProfesorId(), "TEACHER_ID no puede ser null.");
        Objects.requireNonNull(asignacion.getAlumnoId(), "STUDENT_ID no puede ser null.");
        Objects.requireNonNull(asignacion.getHoraInicio(), "START_TIME no puede ser null.");
        Objects.requireNonNull(asignacion.getFechaInicio(), "START_DATE no puede ser null.");
        Objects.requireNonNull(asignacion.getFechaFin(), "END_DATE no puede ser null.");
        if (asignacion.getDiaSemana() < 1 || asignacion.getDiaSemana() > 7) {
            throw new IllegalArgumentException("DAY_OF_WEEK debe estar entre 1 y 7.");
        }
        if (asignacion.getFechaFin().isBefore(asignacion.getFechaInicio())) {
            throw new IllegalArgumentException("END_DATE no puede ser anterior a START_DATE.");
        }
    }

    private Asignacion map(ResultSet rows) throws SQLException {
        return new Asignacion(
                rows.getInt("ASSIGNMENT_ID"),
                rows.getInt("TEACHER_ID"),
                rows.getInt("STUDENT_ID"),
                rows.getInt("DAY_OF_WEEK"),
                LocalTime.parse(rows.getString("START_TIME")),
                LocalDate.parse(rows.getString("START_DATE")),
                LocalDate.parse(rows.getString("END_DATE"))
        );
    }
}
