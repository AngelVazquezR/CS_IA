package com.angelvazquez.csia.database.schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Collection;
import java.util.stream.Collectors;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.ConfiguracionManager;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.DatabaseType;
import com.angelvazquez.csia.database.schema.DatabaseSchema.Column;
import com.angelvazquez.csia.database.schema.DatabaseSchema.ForeignKey;
import com.angelvazquez.csia.database.schema.DatabaseSchema.Index;
import com.angelvazquez.csia.database.schema.DatabaseSchema.Table;
import com.angelvazquez.csia.database.schema.JdbcSchemaExtractor.Extraction;
import com.angelvazquez.csia.database.schema.SchemaComparator.Result;

/**
 * Comando independiente que valida MySQL antes de permitir crear SQLite.
 * No modifica ninguna de las dos bases de datos.
 */
public final class SchemaValidationMain {

    private static final Path REPORT_PATH = Path.of(
            "target", "schema-validation-report.txt");
    private static final Path EXTRACTED_SCHEMA_PATH = Path.of(
            "target", "mysql-schema-extracted.txt");

    private SchemaValidationMain() {
    }

    public static void main(String[] args) {
        int exitCode = run();
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run() {
        try {
            ConfigDB configuration =
                    new ConfiguracionManager().inicializarConfiguracion();

            if (configuration == null) {
                return writeFailure("No se ha podido cargar la configuración.");
            }
            if (configuration.databaseType != DatabaseType.MYSQL) {
                return writeFailure(
                        "La validación debe ejecutarse con <tipo>mysql</tipo> "
                                + "para poder extraer el modelo real de MySQL.");
            }

            String script = loadCanonicalScript();
            DatabaseSchema expected =
                    new MySqlSchemaScriptParser().parse(script);
            Extraction extraction;

            try (Connection connection =
                         new DatabaseConnectionFactory().open(configuration)) {
                extraction = new JdbcSchemaExtractor()
                        .extractWithContext(connection, configuration.db);
            }

            writeExtractedSchema(extraction);
            String context = formatConnectionContext(extraction);

            if (extraction.schema().tables().isEmpty()) {
                String report = context + System.lineSeparator()
                        + System.lineSeparator()
                        + "VALIDACIÓN NO COMPLETADA: no se ha extraído "
                        + "ninguna tabla del catálogo seleccionado. "
                        + "No se comparará ni se creará SQLite.";
                writeReport(report);
                System.err.println(report);
                printOutputPaths();
                return 2;
            }

            Result result = new SchemaComparator().compare(
                    expected, extraction.schema());
            String report = context + System.lineSeparator()
                    + System.lineSeparator() + result.report();
            writeReport(report);
            System.out.println(report);
            printOutputPaths();
            return result.matches() ? 0 : 1;
        } catch (Exception e) {
            e.printStackTrace();
            return writeFailure(
                    "No se ha podido completar la validación: "
                            + e.getMessage());
        }
    }

    private static String loadCanonicalScript() throws IOException {
        try (InputStream stream = SchemaValidationMain.class
                .getResourceAsStream("/modelo.sql")) {
            if (stream != null) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        Path developmentPath = Path.of("assets", "modelo.sql");
        if (Files.isRegularFile(developmentPath)) {
            return Files.readString(developmentPath);
        }
        throw new IOException(
                "No se encuentra el script canónico assets/modelo.sql.");
    }

    static String formatConnectionContext(Extraction extraction) {
        return """
                CONTEXTO DE LA EXTRACCIÓN
                - Motor: %s %s
                - Servidor: %s
                - Catálogo solicitado por la conexión: %s
                - Catálogo seleccionado: %s
                - Catálogos disponibles: %s
                - Tablas encontradas (%d): %s"""
                .formatted(
                        display(extraction.databaseProduct()),
                        display(extraction.databaseVersion()),
                        sanitizeServerUrl(extraction.serverUrl()),
                        display(extraction.requestedCatalog()),
                        display(extraction.selectedCatalog()),
                        joinOrNone(extraction.availableCatalogs()),
                        extraction.schema().tables().size(),
                        joinOrNone(extraction.schema().tables().keySet()));
    }

    static String formatExtractedSchema(Extraction extraction) {
        StringBuilder output = new StringBuilder();
        output.append("MODELO MYSQL EXTRAÍDO MEDIANTE JDBC")
                .append(System.lineSeparator())
                .append(formatConnectionContext(extraction))
                .append(System.lineSeparator());

        for (Table table : extraction.schema().tables().values()) {
            output.append(System.lineSeparator())
                    .append("TABLA ").append(table.name())
                    .append(System.lineSeparator())
                    .append("  COLUMNAS:")
                    .append(System.lineSeparator());

            int position = 1;
            for (String columnName : table.columnOrder()) {
                Column column = table.columns().get(columnName);
                output.append("    ").append(position++).append(". ")
                        .append(column.name()).append(' ')
                        .append(column.type());
                if (column.size() != null) {
                    output.append('(').append(column.size()).append(')');
                }
                output.append(column.nullable() ? " NULL" : " NOT NULL");
                if (column.defaultValue() != null) {
                    output.append(" DEFAULT ")
                            .append(column.defaultValue());
                }
                if (column.autoIncrement()) {
                    output.append(" AUTO_INCREMENT");
                }
                output.append(System.lineSeparator());
            }

            output.append("  CLAVE PRIMARIA: ")
                    .append(joinOrNone(table.primaryKey()))
                    .append(System.lineSeparator())
                    .append("  ÍNDICES:")
                    .append(System.lineSeparator());
            if (table.indexes().isEmpty()) {
                output.append("    (ninguno)")
                        .append(System.lineSeparator());
            } else {
                for (Index index : table.indexes()) {
                    output.append("    ")
                            .append(index.unique() ? "UNIQUE " : "NO UNIQUE ")
                            .append(String.join(", ", index.columns()))
                            .append(System.lineSeparator());
                }
            }

            output.append("  CLAVES FORÁNEAS:")
                    .append(System.lineSeparator());
            if (table.foreignKeys().isEmpty()) {
                output.append("    (ninguna)")
                        .append(System.lineSeparator());
            } else {
                for (ForeignKey foreignKey : table.foreignKeys()) {
                    output.append("    ")
                            .append(String.join(", ", foreignKey.columns()))
                            .append(" -> ")
                            .append(foreignKey.referencedTable())
                            .append('(')
                            .append(String.join(
                                    ", ", foreignKey.referencedColumns()))
                            .append(')')
                            .append(System.lineSeparator());
                }
            }
        }
        return output.toString();
    }

    private static String display(String value) {
        return value == null || value.isBlank() ? "(no disponible)" : value;
    }

    private static String joinOrNone(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "(ninguno)";
        }
        return values.stream().sorted().collect(Collectors.joining(", "));
    }

    private static String sanitizeServerUrl(String url) {
        if (url == null || url.isBlank()) {
            return "(no disponible)";
        }
        return url.replaceAll(
                "(?i)(password|passwd|pwd)=([^&;]+)", "$1=***");
    }

    private static int writeFailure(String message) {
        String report = "VALIDACIÓN NO COMPLETADA: " + message;
        System.err.println(report);
        try {
            writeReport(report);
        } catch (IOException reportError) {
            reportError.printStackTrace();
        }
        return 2;
    }

    private static void writeExtractedSchema(Extraction extraction)
            throws IOException {

        Files.createDirectories(EXTRACTED_SCHEMA_PATH.getParent());
        Files.writeString(
                EXTRACTED_SCHEMA_PATH,
                formatExtractedSchema(extraction),
                StandardCharsets.UTF_8);
    }

    private static void writeReport(String content) throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());
        Files.writeString(
                REPORT_PATH,
                content + System.lineSeparator(),
                StandardCharsets.UTF_8);
    }

    private static void printOutputPaths() {
        System.out.println(
                "Modelo extraído: " + EXTRACTED_SCHEMA_PATH.toAbsolutePath());
        System.out.println("Informe: " + REPORT_PATH.toAbsolutePath());
    }
}
