package com.angelvazquez.csia.database.repository;

import java.sql.SQLException;
import com.angelvazquez.csia.i18n.I18n;

/** Traduce solo las restricciones de negocio conocidas; conserva los demás errores JDBC. */
final class RepositoryErrors {
    private RepositoryErrors() { }

    static SQLException traducir(SQLException error) {
        String mensaje = error.getMessage();
        if (mensaje != null) {
            if (mensaje.contains("CSIA_STUDENTS_DNI_UNIQUE")) {
                throw new IllegalArgumentException(I18n.get("students.duplicateDni"), error);
            }
            if (mensaje.contains("CSIA_TEACHERS_DNI_UNIQUE")) {
                throw new IllegalArgumentException(I18n.get("teachers.duplicateDni"), error);
            }
            if (mensaje.contains("CSIA_SELF_ASSIGNMENT")) {
                throw new IllegalArgumentException(I18n.get("assign.selfAssignment"), error);
            }
        }
        return error;
    }
}
