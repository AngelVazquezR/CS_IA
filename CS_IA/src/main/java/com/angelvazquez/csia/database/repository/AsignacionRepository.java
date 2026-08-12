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

    private final DatabaseConnectionFactory connectionFactory;
    private final ConfigDB configuration;

    public AsignacionRepository(DatabaseConnectionFactory connectionFactory, ConfigDB configuration) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
        this.configuration = Objects.requireNonNull(configuration);
    }

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
        }
    }

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
        }
    }

    public boolean eliminar(int assignmentId) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, assignmentId);
            return statement.executeUpdate() == 1;
        }
    }

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
