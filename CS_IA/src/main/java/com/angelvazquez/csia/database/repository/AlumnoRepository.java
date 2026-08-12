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

    private final DatabaseConnectionFactory connectionFactory;
    private final ConfigDB configuration;

    public AlumnoRepository(DatabaseConnectionFactory connectionFactory, ConfigDB configuration) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
        this.configuration = Objects.requireNonNull(configuration);
    }

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

    public Optional<Alumno> buscarPorDni(String dni) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_BY_DNI)) {
            statement.setString(1, dni);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        }
    }

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

    public boolean eliminar(int studentId) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, studentId);
            return statement.executeUpdate() == 1;
        }
    }

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
