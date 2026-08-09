package com.angelvazquez.csia.database.schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.angelvazquez.csia.database.schema.DatabaseSchema.Column;
import com.angelvazquez.csia.database.schema.DatabaseSchema.ForeignKey;
import com.angelvazquez.csia.database.schema.DatabaseSchema.Index;
import com.angelvazquez.csia.database.schema.DatabaseSchema.Table;

/** Lee las declaraciones CREATE TABLE del script MySQL canónico. */
public final class MySqlSchemaScriptParser {

    private static final Pattern CREATE_TABLE = Pattern.compile(
            "(?i)CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?"
                    + "(?:`[^`]+`\\.)?`?([A-Za-z0-9_]+)`?\\s*\\(");
    private static final Pattern COLUMN = Pattern.compile(
            "^`([^`]+)`\\s+([A-Za-z]+)"
                    + "(?:\\s*\\(\\s*(\\d+)(?:\\s*,\\s*\\d+)?\\s*\\))?"
                    + "(.*)$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern DEFAULT_VALUE = Pattern.compile(
            "(?i)\\bDEFAULT\\s+(('[^']*')|(\"[^\"]*\")|([^\\s,]+))");
    private static final Pattern PRIMARY_KEY = Pattern.compile(
            "(?i)^PRIMARY\\s+KEY\\s*\\((.*)\\)$",
            Pattern.DOTALL);
    private static final Pattern INDEX = Pattern.compile(
            "(?i)^(UNIQUE\\s+)?(?:KEY|INDEX)"
                    + "(?:\\s+`?[^`\\s(]+`?)?\\s*\\((.*)\\)$",
            Pattern.DOTALL);
    private static final Pattern FOREIGN_KEY = Pattern.compile(
            "(?i)^(?:CONSTRAINT\\s+`?[^`\\s]+`?\\s+)?"
                    + "FOREIGN\\s+KEY(?:\\s+`?[^`\\s(]+`?)?\\s*\\((.*?)\\)"
                    + "\\s+REFERENCES\\s+(?:`[^`]+`\\.)?`?([^`\\s(]+)`?"
                    + "\\s*\\((.*?)\\).*$",
            Pattern.DOTALL);

    public DatabaseSchema parse(String script) {
        if (script == null || script.isBlank()) {
            throw new IllegalArgumentException("El script SQL está vacío.");
        }

        Map<String, Table> tables = new LinkedHashMap<>();
        String cleaned = removeComments(script);
        Matcher matcher = CREATE_TABLE.matcher(cleaned);
        int searchFrom = 0;

        while (matcher.find(searchFrom)) {
            String tableName = matcher.group(1);
            int openingParenthesis = matcher.end() - 1;
            int closingParenthesis = findClosingParenthesis(
                    cleaned, openingParenthesis);
            Table table = parseTable(
                    tableName,
                    cleaned.substring(openingParenthesis + 1, closingParenthesis));
            tables.put(tableName, table);
            searchFrom = closingParenthesis + 1;
        }

        if (tables.isEmpty()) {
            throw new IllegalArgumentException(
                    "El script no contiene ninguna sentencia CREATE TABLE.");
        }
        return new DatabaseSchema(tables);
    }

    private Table parseTable(String tableName, String body) {
        Map<String, Column> columns = new LinkedHashMap<>();
        List<String> primaryKey = new ArrayList<>();
        Set<Index> indexes = new TreeSet<>();
        Set<ForeignKey> foreignKeys = new TreeSet<>();

        for (String part : splitTopLevel(body)) {
            String definition = part.trim();
            if (definition.isEmpty()) {
                continue;
            }

            Matcher primaryMatcher = PRIMARY_KEY.matcher(definition);
            Matcher foreignMatcher = FOREIGN_KEY.matcher(definition);
            Matcher indexMatcher = INDEX.matcher(definition);
            Matcher columnMatcher = COLUMN.matcher(definition);

            if (primaryMatcher.matches()) {
                primaryKey = parseIdentifierList(primaryMatcher.group(1));
            } else if (foreignMatcher.matches()) {
                foreignKeys.add(new ForeignKey(
                        parseIdentifierList(foreignMatcher.group(1)),
                        foreignMatcher.group(2),
                        parseIdentifierList(foreignMatcher.group(3))));
            } else if (indexMatcher.matches()) {
                indexes.add(new Index(
                        indexMatcher.group(1) != null,
                        parseIdentifierList(indexMatcher.group(2))));
            } else if (columnMatcher.matches()) {
                String name = columnMatcher.group(1);
                String type = columnMatcher.group(2);
                Integer size = columnMatcher.group(3) == null
                        || !typeSupportsSize(type)
                        ? null
                        : Integer.valueOf(columnMatcher.group(3));
                String attributes = columnMatcher.group(4);
                Matcher defaultMatcher = DEFAULT_VALUE.matcher(attributes);
                String defaultValue = defaultMatcher.find()
                        ? defaultMatcher.group(1)
                        : null;

                columns.put(name, new Column(
                        name,
                        type,
                        size,
                        !Pattern.compile("(?i)\\bNOT\\s+NULL\\b")
                                .matcher(attributes).find(),
                        defaultValue,
                        Pattern.compile("(?i)\\bAUTO_INCREMENT\\b")
                                .matcher(attributes).find()));
            } else {
                throw new IllegalArgumentException(
                        "Definición SQL no reconocida en " + tableName
                                + ": " + definition);
            }
        }

        return new Table(
                tableName,
                columns,
                new ArrayList<>(columns.keySet()),
                primaryKey,
                indexes,
                foreignKeys);
    }

    private String removeComments(String script) {
        return script
                .replaceAll("(?s)/\\*!.*?\\*/\\s*;?", " ")
                .replaceAll("(?m)--[^\\r\\n]*$", " ")
                .replaceAll("(?s)/\\*(?!\\!).*?\\*/", " ");
    }

    private int findClosingParenthesis(String value, int openingPosition) {
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;

        for (int index = openingPosition; index < value.length(); index++) {
            char current = value.charAt(index);
            char previous = index == 0 ? '\0' : value.charAt(index - 1);
            if (current == '\'' && previous != '\\'
                    && !inDoubleQuote && !inBacktick) {
                inSingleQuote = !inSingleQuote;
            } else if (current == '"' && previous != '\\'
                    && !inSingleQuote && !inBacktick) {
                inDoubleQuote = !inDoubleQuote;
            } else if (current == '`' && !inSingleQuote && !inDoubleQuote) {
                inBacktick = !inBacktick;
            } else if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                if (current == '(') {
                    depth++;
                } else if (current == ')' && --depth == 0) {
                    return index;
                }
            }
        }
        throw new IllegalArgumentException(
                "Paréntesis sin cerrar en una sentencia CREATE TABLE.");
    }

    private List<String> splitTopLevel(String value) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean inSingleQuote = false;

        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            char previous = index == 0 ? '\0' : value.charAt(index - 1);
            if (current == '\'' && previous != '\\') {
                inSingleQuote = !inSingleQuote;
            } else if (!inSingleQuote) {
                if (current == '(') {
                    depth++;
                } else if (current == ')') {
                    depth--;
                } else if (current == ',' && depth == 0) {
                    parts.add(value.substring(start, index));
                    start = index + 1;
                }
            }
        }
        parts.add(value.substring(start));
        return parts;
    }

    private List<String> parseIdentifierList(String value) {
        List<String> identifiers = new ArrayList<>();
        for (String item : splitTopLevel(value)) {
            String identifier = item.trim()
                    .replaceAll("^`|`$", "")
                    .replaceAll("\\s+(ASC|DESC)$", "")
                    .replaceAll("\\(\\d+\\)$", "");
            identifiers.add(identifier);
        }
        return identifiers;
    }

    private boolean typeSupportsSize(String type) {
        return Set.of("CHAR", "VARCHAR", "BINARY", "VARBINARY")
                .contains(DatabaseSchema.normalizeIdentifier(type));
    }
}
