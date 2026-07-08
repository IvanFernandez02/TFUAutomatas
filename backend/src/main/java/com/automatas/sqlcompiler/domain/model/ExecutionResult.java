package com.automatas.sqlcompiler.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Resultado de ejecutar una consulta SQL traducida en la base de datos PostgreSQL.
 *
 * @param success      true si la ejecución fue exitosa.
 * @param message      Mensaje descriptivo del resultado (ej. "3 filas encontradas").
 * @param columnNames  Nombres de columnas (solo para SELECT).
 * @param rows         Filas de resultado (solo para SELECT).
 * @param affectedRows Número de filas afectadas (para INSERT/UPDATE/DELETE).
 * @param queryType    Tipo de consulta ejecutada (SELECT, INSERT, UPDATE, DELETE).
 * @param executedSql  Consulta SQL estándar que se ejecutó en PostgreSQL.
 */
public record ExecutionResult(
        boolean success,
        String message,
        List<String> columnNames,
        List<Map<String, Object>> rows,
        int affectedRows,
        String queryType,
        String executedSql
) {
    public static ExecutionResult success(String message, String queryType, String sql,
                                          List<String> columnNames, List<Map<String, Object>> rows) {
        return new ExecutionResult(true, message, columnNames, rows, 0, queryType, sql);
    }

    public static ExecutionResult success(String message, String queryType, String sql, int affectedRows) {
        return new ExecutionResult(true, message, List.of(), List.of(), affectedRows, queryType, sql);
    }

    public static ExecutionResult error(String message, String queryType, String sql) {
        return new ExecutionResult(false, message, List.of(), List.of(), 0, queryType, sql);
    }
}
