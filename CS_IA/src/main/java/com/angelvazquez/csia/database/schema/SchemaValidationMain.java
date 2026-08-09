package com.angelvazquez.csia.database.schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.ConfiguracionManager;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.DatabaseType;
import com.angelvazquez.csia.database.schema.SchemaComparator.Result;

/**
 * Comando independiente que valida MySQL antes de permitir crear SQLite.
 * No modifica ninguna de las dos bases de datos.
 */
public final class SchemaValidationMain {

    private static final Path REPORT_PATH = Path.of(
            "target", "schema-validation-report.txt");

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
            DatabaseSchema actual;

            try (Connection connection =
                         new DatabaseConnectionFactory().open(configuration)) {
                actual = new JdbcSchemaExtractor().extract(connection);
            }

            Result result = new SchemaComparator().compare(expected, actual);
            writeReport(result.report());
            System.out.println(result.report());
            System.out.println("Informe: " + REPORT_PATH.toAbsolutePath());
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

    private static void writeReport(String content) throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());
        Files.writeString(REPORT_PATH, content + System.lineSeparator());
    }
}
