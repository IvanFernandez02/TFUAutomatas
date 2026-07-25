package com.automatas.shared.domain.model;

import java.util.List;
import java.util.Map;

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
