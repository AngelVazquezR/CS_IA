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
import com.angelvazquez.csia.util.Dni;
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
            WHERE UPPER(TRIM(DNI)) = ?
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

    /**
     * Cada operación SQL abre y cierra su propia conexión mediante try-with-resources.
     * Las búsquedas previas no reservan el DNI; los índices protegen cada escritura.
     */
    private final DatabaseConnectionFactory connectionFactory;
    private final ConfigDB configuration;

    public ProfesorRepository(DatabaseConnectionFactory connectionFactory, ConfigDB configuration) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
        this.configuration = Objects.requireNonNull(configuration);
    }

    /**
     * Obtiene todas las filas ordenadas por TEACHER_ID.
     *
     * @return lista de entidades nuevas, vacía si no hay registros; modificarla no persiste cambios
     * @throws SQLException si falla la conexión o la consulta
     */
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

    /**
     * Busca por DNI normalizado (trim y mayúsculas), igual que al guardar.
     *
     * @param dni DNI no vacío; se recorta y convierte a mayúsculas
     * @throws IllegalArgumentException si el DNI es nulo o vacío
     * @return entidad encontrada, o un Optional vacío si no hay coincidencias
     * @throws SQLException si falla la conexión o la consulta
     */
    public Optional<Profesor> buscarPorDni(String dni) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(FIND_BY_DNI)) {
            statement.setString(1, Dni.normalizar(dni));
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        }
    }

    /**
     * Inserta una nueva entidad y asigna a la instancia recibida el ID generado.
     * Normaliza el DNI; el índice único de la base rechaza duplicados en esta tabla.
     * No valida el formato legal del DNI. Se permite el mismo DNI en la otra tabla.
     *
     * @param profesor entidad no nula cuyos datos se van a guardar
     * @return TEACHER_ID generado, también guardado en la entidad
     * @throws IllegalArgumentException si el DNI está vacío o ya existe en esta tabla
     * @throws NullPointerException si la entidad es nula
     * @throws SQLException si falla la inserción o no se obtiene la clave generada
     */
    public int agregar(Profesor profesor) throws SQLException {
        Objects.requireNonNull(profesor);
        String dniNormalizado = Dni.normalizar(profesor.GetDNI());
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, profesor.GetNombre());
            statement.setString(2, profesor.GetApellido());
            statement.setString(3, dniNormalizado);
            statement.setString(4, profesor.getAsignatura());
            statement.setString(5, profesor.getEmail());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se ha obtenido TEACHER_ID al crear el profesor.");
                }
                int id = keys.getInt(1);
                profesor.setDatabaseId(id);
                profesor.DNI = dniNormalizado;
                return id;
            }
        } catch (SQLException e) {
            throw RepositoryErrors.traducir(e);
        }
    }

    /**
     * Actualiza los campos del registro identificado por el ID de la entidad.
     * Normaliza el DNI; el índice único de la base rechaza duplicados en esta tabla.
     * No valida el formato legal del DNI. Se permite el mismo DNI en la otra tabla.
     *
     * @param profesor entidad no nula con identificador de base de datos
     * @return true si JDBC informa de exactamente una fila afectada; false en otro caso
     * @throws NullPointerException si la entidad es nula
     * @throws IllegalArgumentException si falta el identificador, el DNI es inválido,
     *         está duplicado o el cambio crea una autoasignación
     * @throws SQLException si falla la actualización
     */
    public boolean modificar(Profesor profesor) throws SQLException {
        Objects.requireNonNull(profesor);
        String dniNormalizado = Dni.normalizar(profesor.GetDNI());
        if (profesor.getDatabaseId() == null) {
            throw new IllegalArgumentException("El profesor debe tener TEACHER_ID para modificarse.");
        }
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, profesor.GetNombre());
            statement.setString(2, profesor.GetApellido());
            statement.setString(3, dniNormalizado);
            statement.setString(4, profesor.getAsignatura());
            statement.setString(5, profesor.getEmail());
            statement.setInt(6, profesor.getDatabaseId());
            boolean actualizado = statement.executeUpdate() == 1;
            if (actualizado) profesor.DNI = dniNormalizado;
            return actualizado;
        } catch (SQLException e) {
            throw RepositoryErrors.traducir(e);
        }
    }

    /**
     * Elimina el registro indicado; las restricciones de la base de datos pueden impedirlo.
     *
     * @param teacherId identificador del registro que se desea eliminar
     * @return true si JDBC informa de exactamente una fila afectada; false en otro caso
     * @throws SQLException si falla el borrado, incluida una restricción referencial
     */
    public boolean eliminar(int teacherId) throws SQLException {
        try (Connection connection = connectionFactory.open(configuration);
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, teacherId);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Consulta si existe algún registro con el DNI, usando la misma búsqueda de buscarPorDni.
     * El resultado no reserva el DNI; la unicidad se garantiza al escribir mediante el índice.
     *
     * @param dni DNI no vacío; se recorta y convierte a mayúsculas
     * @throws IllegalArgumentException si el DNI es nulo o vacío
     * @return true si la búsqueda encuentra una fila
     * @throws SQLException si falla la consulta
     */
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
