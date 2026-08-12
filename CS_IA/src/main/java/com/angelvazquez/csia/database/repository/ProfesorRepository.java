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

/** Acceso JDBC portable a los datos de profesores. */
public final class ProfesorRepository {

    private static final String FIND_ALL = """
            SELECT ID, NOMBRE, APELLIDOS, DNI, FALTA, FBAJA
            FROM PROFESORES
            ORDER BY ID
            """;

    private static final String FIND_NAMES = """
            SELECT NOMBRE
            FROM PROFESORES
            ORDER BY NOMBRE
            """;

    private static final String FIND_IDS = """
            SELECT ID
            FROM PROFESORES
            """;

    private static final String INSERT = """
            INSERT INTO PROFESORES
                (ID, NOMBRE, APELLIDOS, DNI, FALTA, FBAJA)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE PROFESORES
            SET NOMBRE = ?, APELLIDOS = ?, FALTA = ?, FBAJA = ?
            WHERE DNI = ?
            """;

    private static final String DELETE = """
            DELETE FROM PROFESORES
            WHERE NOMBRE = ? AND APELLIDOS = ? AND DNI = ?
            """;

    private static final String EXISTS_DNI = """
            SELECT DNI
            FROM PROFESORES
            WHERE DNI = ?
            """;

    private final DatabaseConnectionFactory connectionFactory;
    private final ConfigDB configuration;

    public ProfesorRepository(
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

    public List<ProfesorData> listar() throws SQLException {
        List<ProfesorData> profesores = new ArrayList<>();

        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                profesores.add(new ProfesorData(
                        rows.getString("ID"),
                        rows.getString("NOMBRE"),
                        rows.getString("APELLIDOS"),
                        rows.getString("DNI"),
                        rows.getString("FALTA"),
                        rows.getString("FBAJA")
                ));
            }
        }

        return profesores;
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

    public String agregar(
            String nombre,
            String apellidos,
            String dni,
            String fechaAlta,
            String fechaBaja
    ) throws SQLException {

        try (Connection connection = connectionFactory.open(configuration)) {
            String id = siguienteId(connection);

            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                statement.setString(1, id);
                statement.setString(2, nombre);
                statement.setString(3, apellidos);
                statement.setString(4, dni);
                statement.setString(5, fechaAlta);
                statement.setString(6, fechaBaja);
                statement.executeUpdate();
            }

            return id;
        }
    }

    public int modificar(
            String nombre,
            String apellidos,
            String dni,
            String fechaAlta,
            String fechaBaja
    ) throws SQLException {

        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setString(1, nombre);
            statement.setString(2, apellidos);
            statement.setString(3, fechaAlta);
            statement.setString(4, fechaBaja);
            statement.setString(5, dni);

            return statement.executeUpdate();
        }
    }

    public int eliminar(String nombre, String apellidos, String dni)
            throws SQLException {

        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setString(1, nombre);
            statement.setString(2, apellidos);
            statement.setString(3, dni);

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
                        sequenceFrom(rows.getString("ID"), "PR")
                );
            }
        }

        return "PR" + String.format("%04d", maxSequence + 1);
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

    public record ProfesorData(
            String id,
            String nombre,
            String apellidos,
            String dni,
            String fechaAlta,
            String fechaBaja
    ) {
    }
}
