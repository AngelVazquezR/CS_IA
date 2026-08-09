package com.angelvazquez.csia.database.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.angelvazquez.csia.database.schema.DatabaseSchema.Column;
import com.angelvazquez.csia.database.schema.DatabaseSchema.ForeignKey;
import com.angelvazquez.csia.database.schema.DatabaseSchema.Index;
import com.angelvazquez.csia.database.schema.DatabaseSchema.Table;

/** Extrae el modelo real usando exclusivamente metadatos JDBC. */
public final class JdbcSchemaExtractor {

    public DatabaseSchema extract(Connection connection) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        String catalog = connection.getCatalog();
        Map<String, Table> tables = new LinkedHashMap<>();

        try (ResultSet result = metadata.getTables(
                catalog, null, "%", new String[] {"TABLE"})) {
            while (result.next()) {
                String tableName = result.getString("TABLE_NAME");
                tables.put(tableName, extractTable(metadata, catalog, tableName));
            }
        }
        return new DatabaseSchema(tables);
    }

    private Table extractTable(
            DatabaseMetaData metadata,
            String catalog,
            String tableName) throws SQLException {

        Map<String, Column> columns = extractColumns(metadata, catalog, tableName);
        List<String> primaryKey = extractPrimaryKey(metadata, catalog, tableName);
        Set<Index> indexes = extractIndexes(metadata, catalog, tableName);
        Set<ForeignKey> foreignKeys = extractForeignKeys(
                metadata, catalog, tableName);

        return new Table(
                tableName,
                columns,
                new ArrayList<>(columns.keySet()),
                primaryKey,
                indexes,
                foreignKeys);
    }

    private Map<String, Column> extractColumns(
            DatabaseMetaData metadata,
            String catalog,
            String tableName) throws SQLException {

        Map<String, Column> columns = new LinkedHashMap<>();
        try (ResultSet result = metadata.getColumns(
                catalog, null, tableName, "%")) {
            while (result.next()) {
                String name = result.getString("COLUMN_NAME");
                String type = normalizeJdbcType(result.getString("TYPE_NAME"));
                Integer size = typeSupportsSize(type)
                        ? result.getInt("COLUMN_SIZE")
                        : null;
                boolean nullable = result.getInt("NULLABLE")
                        != DatabaseMetaData.columnNoNulls;
                String defaultValue = result.getString("COLUMN_DEF");
                boolean autoIncrement = "YES".equalsIgnoreCase(
                        safeGet(result, "IS_AUTOINCREMENT"));

                columns.put(name, new Column(
                        name, type, size, nullable, defaultValue, autoIncrement));
            }
        }
        return columns;
    }

    private List<String> extractPrimaryKey(
            DatabaseMetaData metadata,
            String catalog,
            String tableName) throws SQLException {

        Map<Short, String> orderedColumns = new HashMap<>();
        try (ResultSet result = metadata.getPrimaryKeys(
                catalog, null, tableName)) {
            while (result.next()) {
                orderedColumns.put(
                        result.getShort("KEY_SEQ"),
                        result.getString("COLUMN_NAME"));
            }
        }
        return orderedColumns.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
    }

    private Set<Index> extractIndexes(
            DatabaseMetaData metadata,
            String catalog,
            String tableName) throws SQLException {

        Map<String, IndexParts> grouped = new HashMap<>();
        try (ResultSet result = metadata.getIndexInfo(
                catalog, null, tableName, false, false)) {
            while (result.next()) {
                String indexName = result.getString("INDEX_NAME");
                String columnName = result.getString("COLUMN_NAME");
                if (indexName == null || columnName == null
                        || "PRIMARY".equalsIgnoreCase(indexName)) {
                    continue;
                }
                boolean unique = !result.getBoolean("NON_UNIQUE");
                IndexParts parts = grouped.computeIfAbsent(
                        indexName,
                        ignored -> new IndexParts(unique));
                parts.columns.put(result.getShort("ORDINAL_POSITION"), columnName);
            }
        }

        Set<Index> indexes = new TreeSet<>();
        grouped.values().forEach(parts -> indexes.add(new Index(
                parts.unique,
                parts.columns.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue)
                        .toList())));
        return indexes;
    }

    private Set<ForeignKey> extractForeignKeys(
            DatabaseMetaData metadata,
            String catalog,
            String tableName) throws SQLException {

        Map<String, ForeignKeyParts> grouped = new HashMap<>();
        try (ResultSet result = metadata.getImportedKeys(
                catalog, null, tableName)) {
            while (result.next()) {
                String name = result.getString("FK_NAME");
                if (name == null) {
                    name = result.getString("PKTABLE_NAME") + ":"
                            + result.getString("FKCOLUMN_NAME");
                }
                String referencedTable = result.getString("PKTABLE_NAME");
                ForeignKeyParts parts = grouped.computeIfAbsent(
                        name,
                        ignored -> new ForeignKeyParts(referencedTable));
                short sequence = result.getShort("KEY_SEQ");
                parts.columns.put(sequence, result.getString("FKCOLUMN_NAME"));
                parts.referencedColumns.put(
                        sequence, result.getString("PKCOLUMN_NAME"));
            }
        }

        Set<ForeignKey> foreignKeys = new TreeSet<>();
        grouped.values().forEach(parts -> foreignKeys.add(new ForeignKey(
                orderedValues(parts.columns),
                parts.referencedTable,
                orderedValues(parts.referencedColumns))));
        return foreignKeys;
    }

    private List<String> orderedValues(Map<Short, String> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
    }

    private String normalizeJdbcType(String typeName) {
        String normalized = DatabaseSchema.normalizeIdentifier(typeName);
        int separator = normalized.indexOf(' ');
        return separator < 0 ? normalized : normalized.substring(0, separator);
    }

    private boolean typeSupportsSize(String type) {
        return Set.of("CHAR", "VARCHAR", "BINARY", "VARBINARY")
                .contains(type);
    }

    private String safeGet(ResultSet result, String column) {
        try {
            return result.getString(column);
        } catch (SQLException ignored) {
            return null;
        }
    }

    private static final class IndexParts {
        private final boolean unique;
        private final Map<Short, String> columns = new HashMap<>();

        private IndexParts(boolean unique) {
            this.unique = unique;
        }
    }

    private static final class ForeignKeyParts {
        private final String referencedTable;
        private final Map<Short, String> columns = new HashMap<>();
        private final Map<Short, String> referencedColumns = new HashMap<>();

        private ForeignKeyParts(String referencedTable) {
            this.referencedTable = referencedTable;
        }
    }

}
