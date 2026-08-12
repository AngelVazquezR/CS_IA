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
import com.angelvazquez.csia.model.Profesor;

/** Persistencia de profesores sobre la tabla TEACHERS. */
public final class ProfesorRepository {

    private static final String FIND_ALL = """
            SELECT TEACHER_ID, FIRST_NAME, LAST_NAME, DNI, SUBJECT, EMAIL
            FROM TEACHERS
            ORDER BY TEACHER_ID
            """;

    private static final String FIND_BY_DNI = """
            SELECT TEACHER_ID, FIRST_NAME, LAST_NAME, DNI, SUBJECT, EMAIL
            FROM TEACHERS
            WHERE DNI = ?
            """;

    private static final String INSERT = """
            INSERT INTO TEACHERS (FIRST_NAME, LAST_NAME, DNI, SUBJECT, EMAIL)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE TEACHERS
            SET FIRST_NAME = ?, LAST_NAME = ?, DNI = ?, SUBJECT = ?, EMAIL = ?
            WHERE TEACHER_ID = ?
            """;

    private static final String DELETE = "DELETE FROM TEACHERS WHERE TEACHER_ID = ?";

    private final DatabaseConnectionFactory connectionFactory;
    private final ConfigDB configuration;

    public ProfesorRepository(DatabaseConnectionFactory connectionFactory, ConfigDB configuration) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
        this.configuration = Objects.requireNonNull(configuration);
    }

    public List<Profesor> listar() throws SQLException {
        List<Profesor> profesores = new ArrayList<>();
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                profesores.add(map(rows));
            }
        }
        return profesores;
    }

    public Optional<Profesor> buscarPorDni(String dni) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_BY_DNI)) {
            statement.setString(1, dni);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        }
    }

    public int agregar(Profesor profesor) throws SQLException {
        Objects.requireNonNull(profesor);
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, profesor.GetNombre());
            statement.setString(2, profesor.GetApellido());
            statement.setString(3, profesor.GetDNI());
            statement.setString(4, profesor.getAsignatura());
            statement.setString(5, profesor.getEmail());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se ha obtenido TEACHER_ID al crear el profesor.");
                }
                int id = keys.getInt(1);
                profesor.setDatabaseId(id);
                return id;
            }
        }
    }

    public boolean modificar(Profesor profesor) throws SQLException {
        Objects.requireNonNull(profesor);
        if (profesor.getDatabaseId() == null) {
            throw new IllegalArgumentException("El profesor debe tener TEACHER_ID para modificarse.");
        }
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, profesor.GetNombre());
            statement.setString(2, profesor.GetApellido());
            statement.setString(3, profesor.GetDNI());
            statement.setString(4, profesor.getAsignatura());
            statement.setString(5, profesor.getEmail());
            statement.setInt(6, profesor.getDatabaseId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int teacherId) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, teacherId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean existeDni(String dni) throws SQLException {
        return buscarPorDni(dni).isPresent();
    }

    private Profesor map(ResultSet rows) throws SQLException {
        return new Profesor(
                rows.getInt("TEACHER_ID"),
                rows.getString("FIRST_NAME"),
                rows.getString("LAST_NAME"),
                rows.getString("DNI"),
                rows.getString("SUBJECT"),
                rows.getString("EMAIL")
        );
    }
}
