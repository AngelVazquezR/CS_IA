package com.angelvazquez.csia.controller;

import java.sql.SQLException;
import java.util.Objects;

import com.angelvazquez.csia.database.repository.AlumnoRepository;
import com.angelvazquez.csia.database.repository.ProfesorRepository;
import com.angelvazquez.csia.tablemodel.AlumnoTableModel;
import com.angelvazquez.csia.tablemodel.ProfesorTableModel;

/**
 * Coordina la carga de alumnos y profesores desde persistencia hacia los
 * modelos Swing, evitando que las ventanas ejecuten SQL directamente.
 */
public final class PersonasTableController {

    private final AlumnoRepository alumnoRepository;
    private final ProfesorRepository profesorRepository;

    public PersonasTableController(
            AlumnoRepository alumnoRepository,
            ProfesorRepository profesorRepository) {
        this.alumnoRepository = Objects.requireNonNull(alumnoRepository);
        this.profesorRepository = Objects.requireNonNull(profesorRepository);
    }

    public void cargarAlumnos(AlumnoTableModel tableModel) throws SQLException {
        Objects.requireNonNull(tableModel);
        tableModel.setData(alumnoRepository.listar());
    }

    public void cargarProfesores(ProfesorTableModel tableModel) throws SQLException {
        Objects.requireNonNull(tableModel);
        tableModel.setData(profesorRepository.listar());
    }
}
