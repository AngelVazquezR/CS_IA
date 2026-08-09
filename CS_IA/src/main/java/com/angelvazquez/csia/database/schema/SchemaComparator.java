package com.angelvazquez.csia.database.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import com.angelvazquez.csia.database.schema.DatabaseSchema.Column;
import com.angelvazquez.csia.database.schema.DatabaseSchema.Table;

/** Compara dos modelos normalizados y explica cada diferencia. */
public final class SchemaComparator {

    public Result compare(DatabaseSchema expected, DatabaseSchema actual) {
        List<String> differences = new ArrayList<>();
        compareNames("tabla", expected.tables(), actual.tables(), differences);

        for (String tableName : expected.tables().keySet()) {
            Table expectedTable = expected.tables().get(tableName);
            Table actualTable = actual.tables().get(tableName);
            if (actualTable == null) {
                continue;
            }

            compareNames(
                    "columna de " + tableName,
                    expectedTable.columns(),
                    actualTable.columns(),
                    differences);
            addDifferenceIfDifferent(
                    differences,
                    tableName + ": orden de columnas",
                    expectedTable.columnOrder(),
                    actualTable.columnOrder());
            compareColumns(expectedTable, actualTable, differences);
            addDifferenceIfDifferent(
                    differences,
                    tableName + ": clave primaria",
                    expectedTable.primaryKey(),
                    actualTable.primaryKey());
            addDifferenceIfDifferent(
                    differences,
                    tableName + ": índices",
                    expectedTable.indexes(),
                    actualTable.indexes());
            addDifferenceIfDifferent(
                    differences,
                    tableName + ": claves foráneas",
                    expectedTable.foreignKeys(),
                    actualTable.foreignKeys());
        }
        return new Result(differences);
    }

    private void compareColumns(
            Table expected,
            Table actual,
            List<String> differences) {

        for (String columnName : expected.columns().keySet()) {
            Column expectedColumn = expected.columns().get(columnName);
            Column actualColumn = actual.columns().get(columnName);
            if (actualColumn == null) {
                continue;
            }
            String location = expected.name() + "." + columnName;
            addDifferenceIfDifferent(
                    differences, location + ": tipo",
                    expectedColumn.type(), actualColumn.type());
            addDifferenceIfDifferent(
                    differences, location + ": tamaño",
                    expectedColumn.size(), actualColumn.size());
            addDifferenceIfDifferent(
                    differences, location + ": permite NULL",
                    expectedColumn.nullable(), actualColumn.nullable());
            addDifferenceIfDifferent(
                    differences, location + ": valor predeterminado",
                    expectedColumn.defaultValue(), actualColumn.defaultValue());
            addDifferenceIfDifferent(
                    differences, location + ": autoincremento",
                    expectedColumn.autoIncrement(), actualColumn.autoIncrement());
        }
    }

    private void compareNames(
            String item,
            Map<String, ?> expected,
            Map<String, ?> actual,
            List<String> differences) {

        TreeSet<String> missing = new TreeSet<>(expected.keySet());
        missing.removeAll(actual.keySet());
        missing.forEach(name -> differences.add(
                "Falta " + item + " en la base real: " + name));

        TreeSet<String> extra = new TreeSet<>(actual.keySet());
        extra.removeAll(expected.keySet());
        extra.forEach(name -> differences.add(
                "Sobra " + item + " en la base real: " + name));
    }

    private void addDifferenceIfDifferent(
            List<String> differences,
            String location,
            Object expected,
            Object actual) {

        if (!Objects.equals(expected, actual)) {
            differences.add(location + " — script: " + expected
                    + "; base real: " + actual);
        }
    }

    public record Result(List<String> differences) {
        public Result {
            differences = List.copyOf(differences);
        }

        public boolean matches() {
            return differences.isEmpty();
        }

        public String report() {
            if (matches()) {
                return "VALIDACIÓN CORRECTA: el modelo real coincide con modelo.sql.";
            }
            return "VALIDACIÓN FALLIDA: se han encontrado "
                    + differences.size() + " diferencia(s):\n- "
                    + String.join("\n- ", differences);
        }
    }
}
