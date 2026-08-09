package com.angelvazquez.csia.database.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Modelo normalizado e independiente del motor de una base de datos. */
public record DatabaseSchema(Map<String, Table> tables) {

    public DatabaseSchema {
        tables = immutableNormalizedMap(tables);
    }

    public record Table(
            String name,
            Map<String, Column> columns,
            List<String> columnOrder,
            List<String> primaryKey,
            Set<Index> indexes,
            Set<ForeignKey> foreignKeys) {

        public Table {
            name = normalizeIdentifier(name);
            columns = immutableNormalizedMap(columns);
            columnOrder = normalizeIdentifiers(columnOrder);
            primaryKey = normalizeIdentifiers(primaryKey);
            indexes = immutableSortedSet(indexes);
            foreignKeys = immutableSortedSet(foreignKeys);
        }
    }

    public record Column(
            String name,
            String type,
            Integer size,
            boolean nullable,
            String defaultValue,
            boolean autoIncrement) {

        public Column {
            name = normalizeIdentifier(name);
            type = normalizeIdentifier(type);
            defaultValue = normalizeDefault(defaultValue);
        }
    }

    /** El nombre del índice se omite deliberadamente: no cambia el modelo. */
    public record Index(boolean unique, List<String> columns)
            implements Comparable<Index> {

        public Index {
            columns = normalizeIdentifiers(columns);
        }

        @Override
        public int compareTo(Index other) {
            int uniqueComparison = Boolean.compare(unique, other.unique);
            return uniqueComparison != 0
                    ? uniqueComparison
                    : String.join("\u0000", columns).compareTo(
                            String.join("\u0000", other.columns));
        }
    }

    public record ForeignKey(
            List<String> columns,
            String referencedTable,
            List<String> referencedColumns)
            implements Comparable<ForeignKey> {

        public ForeignKey {
            columns = normalizeIdentifiers(columns);
            referencedTable = normalizeIdentifier(referencedTable);
            referencedColumns = normalizeIdentifiers(referencedColumns);
        }

        @Override
        public int compareTo(ForeignKey other) {
            return toString().compareTo(other.toString());
        }
    }

    private static <T> Map<String, T> immutableNormalizedMap(
            Map<String, T> source) {

        Objects.requireNonNull(source, "El mapa no puede ser null.");
        Map<String, T> normalized = new TreeMap<>();
        source.forEach((key, value) -> normalized.put(
                normalizeIdentifier(key),
                Objects.requireNonNull(value)));
        return Collections.unmodifiableMap(new LinkedHashMap<>(normalized));
    }

    private static List<String> normalizeIdentifiers(
            Collection<String> identifiers) {

        Objects.requireNonNull(identifiers, "La colección no puede ser null.");
        List<String> normalized = new ArrayList<>();
        identifiers.forEach(value -> normalized.add(normalizeIdentifier(value)));
        return List.copyOf(normalized);
    }

    private static <T extends Comparable<? super T>> Set<T> immutableSortedSet(
            Collection<T> source) {

        Objects.requireNonNull(source, "La colección no puede ser null.");
        return Collections.unmodifiableSet(new TreeSet<>(source));
    }

    public static String normalizeIdentifier(String value) {
        return Objects.requireNonNull(value, "El identificador no puede ser null.")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    public static String normalizeDefault(String value) {
        if (value == null || value.isBlank()
                || "NULL".equalsIgnoreCase(value.trim())) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.length() >= 2
                && ((normalized.startsWith("'") && normalized.endsWith("'"))
                || (normalized.startsWith("\"") && normalized.endsWith("\"")))) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized.toUpperCase(Locale.ROOT);
    }
}
