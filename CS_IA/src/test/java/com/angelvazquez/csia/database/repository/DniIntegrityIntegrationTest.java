package com.angelvazquez.csia.database.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.sql.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.angelvazquez.csia.database.*;
import com.angelvazquez.csia.model.*;

class DniIntegrityIntegrationTest {
    @TempDir Path temporal;

    private ConfigDB config() {
        ConfigDB c = new ConfigDB();
        c.databaseType = DatabaseType.SQLITE;
        c.driver = "org.sqlite.JDBC";
        c.url = "jdbc:sqlite:" + temporal.resolve("dni.db");
        return c;
    }

    private AlumnoRepository alumnos() { return new AlumnoRepository(new DatabaseConnectionFactory(), config()); }
    private ProfesorRepository profesores() { return new ProfesorRepository(new DatabaseConnectionFactory(), config()); }
    private AsignacionRepository asignaciones() { return new AsignacionRepository(new DatabaseConnectionFactory(), config()); }

    private Persona persona(boolean alumno, String dni) {
        return alumno ? new Alumno("Ana", "Ruiz", dni, "a@test.example")
                : new Profesor("Ana", "Ruiz", dni, "Matemáticas", "a@test.example");
    }
    private int agregar(boolean alumno, Persona p) throws SQLException {
        return alumno ? alumnos().agregar((Alumno) p) : profesores().agregar((Profesor) p);
    }
    private boolean modificar(boolean alumno, Persona p) throws SQLException {
        return alumno ? alumnos().modificar((Alumno) p) : profesores().modificar((Profesor) p);
    }
    private Persona buscar(boolean alumno, String dni) throws SQLException {
        return alumno ? alumnos().buscarPorDni(dni).orElseThrow() : profesores().buscarPorDni(dni).orElseThrow();
    }
    private Asignacion asignacion(int teacherId, int studentId) {
        return new Asignacion(teacherId, studentId, 1, LocalTime.of(9, 0),
                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30));
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void normalizaAltaBusquedaYRechazaDuplicado(boolean alumno) throws Exception {
        Persona p = persona(alumno, " 12345678z ");
        int id = agregar(alumno, p);
        assertEquals("12345678Z", p.GetDNI());
        assertEquals(id, buscar(alumno, " 12345678z ").getDatabaseId());
        assertThrows(IllegalArgumentException.class, () -> agregar(alumno, persona(alumno, "12345678Z")));
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void permiteConservarDniPropioYNormalizaCambio(boolean alumno) throws Exception {
        Persona p = persona(alumno, "00000001R"); agregar(alumno, p);
        p.DNI = " 00000001r "; assertTrue(modificar(alumno, p));
        p.DNI = " 00000002W "; assertTrue(modificar(alumno, p));
        assertEquals("00000002W", p.GetDNI());
        assertEquals(p.getDatabaseId(), buscar(alumno, " 00000002w ").getDatabaseId());
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void rechazaDniAjenoAlEditarYConservaLaFilaPersistida(boolean alumno) throws Exception {
        Persona a = persona(alumno, "00000001R"); Persona b = persona(alumno, "00000002W");
        agregar(alumno, a); agregar(alumno, b);
        b.DNI = " 00000001r "; b.SetNombre("No guardar");
        assertThrows(IllegalArgumentException.class, () -> modificar(alumno, b));
        assertEquals("Ana", buscar(alumno, "00000002W").GetNombre());
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void rechazaDniVacioSinPersistir(boolean alumno) {
        assertThrows(IllegalArgumentException.class, () -> agregar(alumno, persona(alumno, "  ")));
    }

    @Test void permiteMismoDniEnAmbasTablasPeroRechazaAutoasignacion() throws Exception {
        Alumno a = (Alumno) persona(true, " x1234567l "); Profesor p = (Profesor) persona(false, "X1234567L");
        int aid = alumnos().agregar(a); int pid = profesores().agregar(p);
        assertThrows(IllegalArgumentException.class, () -> asignaciones().agregar(asignacion(pid, aid)));
        assertTrue(asignaciones().listar().isEmpty());
    }

    @Test void idsIgualesConDniDistintoSonPersonasDistintas() throws Exception {
        int aid = alumnos().agregar((Alumno) persona(true, "00000001R"));
        int pid = profesores().agregar((Profesor) persona(false, "00000002W"));
        assertEquals(aid, pid);
        assertTrue(asignaciones().agregar(asignacion(pid, aid)) > 0);
    }

    @Test void rechazaAutoasignacionAlModificarSinCambiarRegistroAnterior() throws Exception {
        int aid = alumnos().agregar((Alumno) persona(true, "00000001R"));
        int propio = alumnos().agregar((Alumno) persona(true, "00000002W"));
        int pid = profesores().agregar((Profesor) persona(false, "00000002W"));
        Asignacion a = asignacion(pid, aid); asignaciones().agregar(a);
        a.setAlumnoId(propio);
        assertThrows(IllegalArgumentException.class, () -> asignaciones().modificar(a));
        assertEquals(aid, asignaciones().listar().getFirst().getAlumnoId());
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void cambiarDniNoPuedeConvertirAsignacionEnAutoasignacion(boolean alumno) throws Exception {
        Alumno a = (Alumno) persona(true, "00000001R"); Profesor p = (Profesor) persona(false, "00000002W");
        alumnos().agregar(a); profesores().agregar(p);
        asignaciones().agregar(asignacion(p.getDatabaseId(), a.getDatabaseId()));
        Persona editada = alumno ? a : p; editada.DNI = alumno ? " 00000002w " : " 00000001r ";
        assertThrows(IllegalArgumentException.class, () -> modificar(alumno, editada));
        assertEquals(alumno ? "00000001R" : "00000002W", buscar(alumno, alumno ? "00000001R" : "00000002W").GetDNI());
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void indiceProtegeTambienInsercionesSqlDirectas(boolean alumno) throws Exception {
        agregar(alumno, persona(alumno, "00000001R"));
        String sql = alumno ? "INSERT INTO STUDENTS(FIRST_NAME,LAST_NAME,DNI,EMAIL) VALUES('X','Y',' 00000001r ','x')"
                : "INSERT INTO TEACHERS(FIRST_NAME,LAST_NAME,DNI,SUBJECT,EMAIL) VALUES('X','Y',' 00000001r ','M','x')";
        try (Connection c = new DatabaseConnectionFactory().open(config()); Statement st = c.createStatement()) {
            assertThrows(SQLException.class, () -> st.executeUpdate(sql));
        }
    }

    @Test void triggerProtegeSqlDirectoYReaperturaConservaDatos() throws Exception {
        int aid = alumnos().agregar((Alumno) persona(true, "00000001R"));
        int pid = profesores().agregar((Profesor) persona(false, "00000001R"));
        try (Connection c = new DatabaseConnectionFactory().open(config()); Statement st = c.createStatement()) {
            String sql = "INSERT INTO ASSIGNMENTS(TEACHER_ID,STUDENT_ID,DAY_OF_WEEK,START_TIME,START_DATE,END_DATE) VALUES("
                    + pid + "," + aid + ",1,'09:00','2026-09-01','2027-06-30')";
            assertThrows(SQLException.class, () -> st.executeUpdate(sql));
        }
        assertEquals(1, alumnos().listar().size());
        assertEquals(1, profesores().listar().size());
    }
    @ParameterizedTest @ValueSource(booleans = {true, false})
    void duplicadoTienePrioridadSobreAutoasignacionEnBaseExistente(boolean alumno) throws Exception {
        Alumno a = (Alumno) persona(true, "00000001R"); Profesor p = (Profesor) persona(false, "00000002W");
        alumnos().agregar(a); profesores().agregar(p);
        asignaciones().agregar(asignacion(p.getDatabaseId(), a.getDatabaseId()));
        agregar(alumno, persona(alumno, alumno ? "00000002W" : "00000001R"));
        // Simula el trigger anterior en una base ya utilizada con la primera versión.
        String tabla = alumno ? "STUDENTS" : "TEACHERS";
        try (Connection c = new DatabaseConnectionFactory().open(config()); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TRIGGER CSIA_" + tabla + "_SELF_UPDATE BEFORE UPDATE OF DNI ON "
                    + tabla + " BEGIN SELECT RAISE(ABORT, 'CSIA_SELF_ASSIGNMENT'); END");
        }
        Persona editada = alumno ? a : p; editada.DNI = alumno ? " 00000002w " : " 00000001r ";
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> modificar(alumno, editada));
        assertEquals(com.angelvazquez.csia.i18n.I18n.get(alumno ? "students.duplicateDni" : "teachers.duplicateDni"),
                error.getMessage());
        assertEquals(editada.getDatabaseId(), buscar(alumno, alumno ? "00000001R" : "00000002W").getDatabaseId());
        assertEquals(1, asignaciones().listar().size());
    }
    @ParameterizedTest @ValueSource(booleans = {true, false})
    void documentoInvalidoNoSeGuardaNiModifica(boolean alumno) throws Exception {
        assertThrows(IllegalArgumentException.class, () -> agregar(alumno, persona(alumno, "12345678A")));
        Persona p = persona(alumno, "X1234567L"); agregar(alumno, p);
        p.DNI = "Y1234567A";
        assertThrows(IllegalArgumentException.class, () -> modificar(alumno, p));
        assertEquals(p.getDatabaseId(), buscar(alumno, "X1234567L").getDatabaseId());
    }
    @ParameterizedTest @ValueSource(booleans = {true, false})
    void nieRespetaUnicidadYPermiteCambiarDesdeDni(boolean alumno) throws Exception {
        Persona p = persona(alumno, "00000001R"); agregar(alumno, p);
        p.DNI = " x1234567l "; assertTrue(modificar(alumno, p));
        assertEquals("X1234567L", p.GetDNI());
        assertThrows(IllegalArgumentException.class, () -> agregar(alumno, persona(alumno, " X1234567l ")));
    }
}
