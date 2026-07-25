package com.automatas.semantico.infrastructure.adapter.out.persistence;

import com.automatas.shared.domain.model.ExecutionResult;
import com.automatas.shared.domain.model.ast.AstNode;
import com.automatas.shared.domain.port.out.DatabasePort;
import com.automatas.shared.domain.port.out.SchemaRepositoryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PostgresDatabaseAdapter implements DatabasePort {

    private final JdbcTemplate jdbcTemplate;
    private final SqlTranslator sqlTranslator;
    private final SchemaRepositoryPort schemaRepository;

    public PostgresDatabaseAdapter(JdbcTemplate jdbcTemplate,
                                    SqlTranslator sqlTranslator,
                                    SchemaRepositoryPort schemaRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlTranslator = sqlTranslator;
        this.schemaRepository = schemaRepository;
    }

    @Override
    public ExecutionResult execute(AstNode node) {
        SqlTranslator.TranslatedSql translated = sqlTranslator.translate(node);
        String sql = translated.sql();
        Object[] params = translated.params().toArray();

        try {
            return switch (node.type()) {
                case "SELECT" -> executeSelect(sql, params);
                case "INSERT" -> executeUpdate(sql, params, "INSERT", "Insercion exitosa");
                case "UPDATE" -> executeUpdate(sql, params, "UPDATE", "Modificacion exitosa");
                case "DELETE" -> executeUpdate(sql, params, "DELETE", "Eliminacion exitosa");
                default -> ExecutionResult.error("Tipo de operacion no soportado", node.type(), sql);
            };
        } catch (Exception ex) {
            return ExecutionResult.error(
                    "Error al ejecutar en PostgreSQL: " + ex.getMessage(),
                    node.type(),
                    sql
            );
        }
    }

    @Override
    public List<Map<String, Object>> queryTable(String tableName) {
        if (!schemaRepository.tableExists(tableName)) {
            return List.of();
        }
        try {
            return jdbcTemplate.queryForList("SELECT * FROM " + tableName + " ORDER BY id");
        } catch (Exception ex) {
            return List.of();
        }
    }

    private ExecutionResult executeSelect(String sql, Object[] params) {
        List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(sql, params);

        List<String> columnNames = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        if (!rawRows.isEmpty()) {
            columnNames.addAll(rawRows.get(0).keySet());
            for (Map<String, Object> rawRow : rawRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String col : columnNames) {
                    row.put(col, rawRow.get(col));
                }
                rows.add(row);
            }
        }

        String message = rows.size() + (rows.size() == 1 ? " fila encontrada" : " filas encontradas");
        return ExecutionResult.success(message, "SELECT", sql, columnNames, rows);
    }

    private ExecutionResult executeUpdate(String sql, Object[] params, String type, String baseMessage) {
        int affected = jdbcTemplate.update(sql, params);
        String message = baseMessage + ": " + affected
                + (affected == 1 ? " fila afectada" : " filas afectadas");
        return ExecutionResult.success(message, type, sql, affected);
    }
}
