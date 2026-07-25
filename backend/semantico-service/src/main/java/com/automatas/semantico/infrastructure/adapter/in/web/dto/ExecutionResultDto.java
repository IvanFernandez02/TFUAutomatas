package com.automatas.semantico.infrastructure.adapter.in.web.dto;

import com.automatas.shared.domain.model.ExecutionResult;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ExecutionResultDto(
        boolean success,
        String message,
        @JsonProperty("column_names") List<String> columnNames,
        List<Map<String, Object>> rows,
        @JsonProperty("affected_rows") int affectedRows,
        @JsonProperty("query_type") String queryType,
        @JsonProperty("executed_sql") String executedSql
) {
    public static ExecutionResultDto from(ExecutionResult result) {
        if (result == null) return null;
        return new ExecutionResultDto(
                result.success(),
                result.message(),
                result.columnNames(),
                result.rows(),
                result.affectedRows(),
                result.queryType(),
                result.executedSql()
        );
    }
}
