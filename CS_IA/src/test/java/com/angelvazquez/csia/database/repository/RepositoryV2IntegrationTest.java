package com.angelvazquez.csia.database.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.DatabaseType;
import com.angelvazquez.csia.model.Alumno;
import com.angelvazquez.csia.model.Asignacion;
import com.angelvazquez.csia.model.Profesor;
import com.angelvazquez.csia.model.Usuario;

class RepositoryV2IntegrationTest {

    @TempDir
    Path tempDir;

    private ConfigDB config;
    private DatabaseConnectionFactory factory;

    @BeforeEach
    void prepararBaseDeDatos() throws Exception {
        config = new ConfigDB();
        config.databaseType = DatabaseType.SQLITE;
        config.driver = "org.sqlite.JDBC";
        config.url = "jdbc:sqlite:" + tempDir.resolve("repository-v2.db");
        factory = new DatabaseConnectionFactory();

        try (Connection connection = factory.open(config)) {
            ejecutarEsquema(connection);
        }
    }

    @Test
    void alumnoRepositoryRealizaCrudConIdAutoincremental() throws Exception {
        AlumnoRepository repository = new AlumnoRepository(factory, config);
        Alumno alumno = new Alumno("Ana", "Lopez", "12345678A", "ana@example.test");

        int id = repository.agregar(alumno);
        assertTrue(id > 0);
        assertEquals(id, alumno.getDatabaseId());
        assertTrue(repository.existeDni("12345678A"));

        Alumno guardado = repository.buscarPorDni("12345678A").orElseThrow();
        assertEquals("Ana", guardado.GetNombre());
        assertEquals("ana@example.test", guardado.getEmail());

        guardado.SetNombre("Ana Maria");
        guardado.setEmail("anamaria@example.test");
        assertTrue(repository.modificar(guardado));
        assertEquals("Ana Maria", repository.listar().getFirst().GetNombre());

        assertTrue(repository.eliminar(id));
        assertFalse(repository.existeDni("12345678A"));
    }

    @Test
    void profesorRepositoryRealizaCrudConSubjectYEmail() throws Exception {
        ProfesorRepository repository = new ProfesorRepository(factory, config);
        Profesor profesor = new Profesor("Luis", "Martin", "87654321B", "Matematicas", "luis@example.test");

        int id = repository.agregar(profesor);
        assertTrue(id > 0);
        assertEquals(id, profesor.getDatabaseId());

        Profesor guardado = repository.buscarPorDni("87654321B").orElseThrow();
        assertEquals("Matematicas", guardado.getAsignatura());
        assertEquals("luis@example.test", guardado.getEmail());

        guardado.setAsignatura("Fisica");
        assertTrue(repository.modificar(guardado));
        assertEquals("Fisica", repository.listar().getFirst().getAsignatura());

        assertTrue(repository.eliminar(id));
        assertFalse(repository.existeDni("87654321B"));
    }

    @Test
    void usuarioRepositoryNormalizaUsernameYPreservaPasswordHash() throws Exception {
        UsuarioRepository repository = new UsuarioRepository(factory, config);
        Usuario usuario = new Usuario(" angel ", "hash-de-prueba");

        int id = repository.registrar(usuario);
        assertTrue(id > 0);
        assertEquals("ANGEL", usuario.getUsername());

        Usuario guardado = repository.buscarPorUsername("angel").orElseThrow();
        assertEquals(id, guardado.getId());
        assertEquals("hash-de-prueba", guardado.getPasswordHash());

        assertThrows(IllegalArgumentException.class,
                () -> repository.registrar(new Usuario("ANGEL", "otro-hash")));
    }

    @Test
    void asignacionRepositoryRespetaClavesForaneasYRealizaCrud() throws Exception {
        AlumnoRepository alumnos = new AlumnoRepository(factory, config);
        ProfesorRepository profesores = new ProfesorRepository(factory, config);
        AsignacionRepository asignaciones = new AsignacionRepository(factory, config);

        Alumno alumno = new Alumno("Eva", "Ruiz", "11111111C", "eva@example.test");
        Profesor profesor = new Profesor("Mario", "Diaz", "22222222D", "Lengua", "mario@example.test");
        int studentId = alumnos.agregar(alumno);
        int teacherId = profesores.agregar(profesor);

        Asignacion invalida = new Asignacion(99999, studentId, 1,
                LocalTime.of(9, 0), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 20));
        assertThrows(SQLException.class, () -> asignaciones.agregar(invalida));

        Asignacion asignacion = new Asignacion(teacherId, studentId, 1,
                LocalTime.of(9, 0), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 20));
        int id = asignaciones.agregar(asignacion);
        assertNotNull(asignacion.getId());
        assertEquals(id, asignacion.getId());
        assertEquals(1, asignaciones.listar().size());

        asignacion.setDiaSemana(3);
        asignacion.setHoraInicio(LocalTime.of(10, 30));
        assertTrue(asignaciones.modificar(asignacion));
        assertEquals(3, asignaciones.listar().getFirst().getDiaSemana());
        assertEquals(LocalTime.of(10, 30), asignaciones.listar().getFirst().getHoraInicio());

        assertTrue(asignaciones.eliminar(id));
        assertTrue(asignaciones.listar().isEmpty());
    }

    private void ejecutarEsquema(Connection connection) throws Exception {
        String sql;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("modelo_sqlite.sql")) {
            if (input == null) {
                throw new IllegalStateException("No se encuentra modelo_sqlite.sql.");
            }
            sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        try (Statement statement = connection.createStatement()) {
            for (String sentencia : sql.split(";")) {
                String limpia = sentencia.trim();
                if (!limpia.isEmpty()) {
                    statement.execute(limpia);
                }
            }
        }
    }
}
