package com.angelvazquez.csia.database.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.angelvazquez.csia.database.schema.JdbcSchemaExtractor.Extraction;

class JdbcSchemaExtractorTest {

    @Test
    void extraeElModeloMedianteMetadatosJdbc() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    CREATE TABLE PERSONAS (
                        ID VARCHAR(6) NOT NULL PRIMARY KEY,
                        NOMBRE VARCHAR(45),
                        UNIQUE (NOMBRE)
                    )
                    """);

            Extraction extraction =
                    new JdbcSchemaExtractor().extractWithContext(connection);
            DatabaseSchema schema = extraction.schema();
            DatabaseSchema.Table table = schema.tables().get("PERSONAS");

            assertTrue(schema.tables().containsKey("PERSONAS"));
            assertEquals(List.of("ID", "NOMBRE"), table.columnOrder());
            assertFalse(table.columns().get("ID").nullable());
            assertTrue(table.columns().get("NOMBRE").nullable());
            assertEquals(List.of("ID"), table.primaryKey());

            String exported =
                    SchemaValidationMain.formatExtractedSchema(extraction);
            assertTrue(exported.contains("TABLA PERSONAS"));
            assertTrue(exported.contains("Tablas encontradas (1): PERSONAS"));
        }
    }

    @Test
    void limitaLaExtraccionAlCatalogoConfiguradoSinCatalogoActivo()
            throws Exception {

        AtomicReference<String> catalogUsed = new AtomicReference<>();
        DatabaseMetaData metadata = proxy(DatabaseMetaData.class,
                (method, arguments) -> switch (method) {
                    case "getCatalogs" -> resultSet(List.of(
                            Map.of("TABLE_CAT", "information_schema"),
                            Map.of("TABLE_CAT", "csia"),
                            Map.of("TABLE_CAT", "world")));
                    case "getTables" -> {
                        catalogUsed.set((String) arguments[0]);
                        yield resultSet(List.of());
                    }
                    case "getDatabaseProductName" -> "MySQL";
                    case "getDatabaseProductVersion" -> "9.4.0";
                    case "getURL" -> "jdbc:mysql://localhost:3306/";
                    default -> null;
                });
        Connection connection = proxy(Connection.class,
                (method, arguments) -> switch (method) {
                    case "getMetaData" -> metadata;
                    case "getCatalog" -> null;
                    default -> null;
                });

        Extraction extraction = new JdbcSchemaExtractor()
                .extractWithContext(connection, "CSIA");

        assertEquals("CSIA", extraction.requestedCatalog());
        assertEquals("csia", extraction.selectedCatalog());
        assertEquals("csia", catalogUsed.get());
    }

    @Test
    void resuelveElCatalogoSinDistinguirMayusculas() throws Exception {
        String selected = JdbcSchemaExtractor.chooseCatalog(
                "CSIA",
                List.of("information_schema", "csia", "mysql"));

        assertEquals("csia", selected);
    }
    private static ResultSet resultSet(List<Map<String, String>> rows) {
        AtomicInteger position = new AtomicInteger(-1);
        return proxy(ResultSet.class, (method, arguments) -> switch (method) {
            case "next" -> position.incrementAndGet() < rows.size();
            case "getString" -> rows.get(position.get())
                    .get((String) arguments[0]);
            default -> null;
        });
    }

    private static <T> T proxy(Class<T> type, Invocation invocation) {
        Object value = Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[] {type},
                (ignored, method, arguments) -> {
                    Object result = invocation.invoke(
                            method.getName(),
                            arguments == null ? new Object[0] : arguments);
                    if (result != null || !method.getReturnType().isPrimitive()) {
                        return result;
                    }
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    if (method.getReturnType() == char.class) {
                        return '\0';
                    }
                    return 0;
                });
        return type.cast(value);
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(String method, Object[] arguments) throws Exception;
    }
}
