package com.angelvazquez.csia.database.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;

/** Acceso JDBC portable a los datos de alumnos. */
public final class AlumnoRepository {

    private static final String FIND_ALL = """
            SELECT ID, NOMBRE, APELLIDO, DNI, PROFESOR
            FROM ALUMNOS
            ORDER BY ID
            """;

    private static final String FIND_NAMES = """
            SELECT NOMBRE
            FROM ALUMNOS
            ORDER BY NOMBRE
            """;

    private static final String FIND_IDS = """
            SELECT ID
            FROM ALUMNOS
            """;

    private static final String INSERT = """
            INSERT INTO ALUMNOS (ID, NOMBRE, APELLIDO, DNI)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE ALUMNOS
            SET NOMBRE = ?, APELLIDO = ?
            WHERE DNI = ?
            """;

    private static final String DELETE = """
            DELETE FROM ALUMNOS
            WHERE NOMBRE = ? AND APELLIDO = ? AND DNI = ?
            """;

    private static final String ASSIGN_TEACHER = """
            UPDATE ALUMNOS
            SET PROFESOR = ?
            WHERE NOMBRE = ?
            """;

    private static final String EXISTS_DNI = """
            SELECT DNI
            FROM ALUMNOS
            WHERE DNI = ?
            """;

    private final DatabaseConnectionFactory connectionFactory;
    private final ConfigDB configuration;

    public AlumnoRepository(
            DatabaseConnectionFactory connectionFactory,
            ConfigDB configuration
    ) {
        this.connectionFactory = Objects.requireNonNull(
                connectionFactory,
                "La fábrica de conexiones no puede ser null."
        );
        this.configuration = Objects.requireNonNull(
                configuration,
                "La configuración de base de datos no puede ser null."
        );
    }

    public List<AlumnoData> listar() throws SQLException {
        List<AlumnoData> alumnos = new ArrayList<>();

        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                alumnos.add(new AlumnoData(
                        rows.getString("ID"),
                        rows.getString("NOMBRE"),
                        rows.getString("APELLIDO"),
                        rows.getString("DNI"),
                        rows.getString("PROFESOR")
                ));
            }
        }

        return alumnos;
    }

    public List<String> listarNombres() throws SQLException {
        List<String> nombres = new ArrayList<>();

        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_NAMES);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                nombres.add(rows.getString("NOMBRE"));
            }
        }

        return nombres;
    }

    public String agregar(String nombre, String apellido, String dni)
            throws SQLException {

        try (Connection connection = connectionFactory.open(configuration)) {
            String id = siguienteId(connection);

            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                statement.setString(1, id);
                statement.setString(2, nombre);
                statement.setString(3, apellido);
                statement.setString(4, dni);
                statement.executeUpdate();
            }

            return id;
        }
    }

    public int modificar(String nombre, String apellido, String dni)
            throws SQLException {

        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.setString(3, dni);

            return statement.executeUpdate();
        }
    }

    public int eliminar(String nombre, String apellido, String dni)
            throws SQLException {

        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.setString(3, dni);

            return statement.executeUpdate();
        }
    }

    public int asignarProfesor(String profesor, String alumno)
            throws SQLException {

        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement =
                     connection.prepareStatement(ASSIGN_TEACHER)) {

            statement.setString(1, profesor);
            statement.setString(2, alumno);

            return statement.executeUpdate();
        }
    }

    public boolean existeDni(String dni) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(EXISTS_DNI)) {

            statement.setString(1, dni);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    private String siguienteId(Connection connection) throws SQLException {
        int maxSequence = 0;

        try (PreparedStatement statement = connection.prepareStatement(FIND_IDS);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                maxSequence = Math.max(
                        maxSequence,
                        sequenceFrom(rows.getString("ID"), "AL")
                );
            }
        }

        return "AL" + String.format("%04d", maxSequence + 1);
    }

    private int sequenceFrom(String id, String prefix) {
        if (id == null || !id.startsWith(prefix)) {
            return 0;
        }

        try {
            return Integer.parseInt(id.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public record AlumnoData(
            String id,
            String nombre,
            String apellido,
            String dni,
            String profesor
    ) {
    }
}
