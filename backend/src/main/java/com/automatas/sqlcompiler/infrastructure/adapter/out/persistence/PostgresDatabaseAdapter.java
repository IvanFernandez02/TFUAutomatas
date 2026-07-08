package com.automatas.sqlcompiler.infrastructure.adapter.out.persistence;

import com.automatas.sqlcompiler.domain.model.ExecutionResult;
import com.automatas.sqlcompiler.domain.model.ast.AstNode;
import com.automatas.sqlcompiler.domain.port.out.DatabasePort;
import com.automatas.sqlcompiler.domain.port.out.SchemaRepositoryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador de infraestructura que ejecuta consultas SQL en PostgreSQL.
 * Implementa el puerto de salida {@link DatabasePort} usando {@link JdbcTemplate}.
 *
 * El flujo es:
 * 1. Recibe un nodo AST ya validado semánticamente.
 * 2. Usa {@link SqlTranslator} para convertirlo a SQL estándar parametrizado.
 * 3. Ejecuta la consulta SQL en PostgreSQL con JdbcTemplate.
 * 4. Retorna el resultado encapsulado en {@link ExecutionResult}.
 */
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
                case "INSERT" -> executeUpdate(sql, params, "INSERT", "Inserción exitosa");
                case "UPDATE" -> executeUpdate(sql, params, "UPDATE", "Modificación exitosa");
                case "DELETE" -> executeUpdate(sql, params, "DELETE", "Eliminación exitosa");
                default -> ExecutionResult.error("Tipo de operación no soportado", node.type(), sql);
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
        // Validar que la tabla exista en el esquema antes de consultar
        if (!schemaRepository.tableExists(tableName)) {
            return List.of();
        }
        try {
            return jdbcTemplate.queryForList("SELECT * FROM " + tableName + " ORDER BY id");
        } catch (Exception ex) {
            return List.of();
        }
    }

    /**
     * Ejecuta una consulta SELECT y retorna las columnas y filas.
     */
    private ExecutionResult executeSelect(String sql, Object[] params) {
        List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(sql, params);

        // Obtener nombres de columnas desde la primera fila, o del SQL
        List<String> columnNames = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        if (!rawRows.isEmpty()) {
            columnNames.addAll(rawRows.get(0).keySet());
            for (Map<String, Object> rawRow : rawRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String col : columnNames) {
                    Object value = rawRow.get(col);
                    row.put(col, value != null ? value : null);
                }
                rows.add(row);
            }
        }

        String message = rows.size() + (rows.size() == 1 ? " fila encontrada" : " filas encontradas");
        return ExecutionResult.success(message, "SELECT", sql, columnNames, rows);
    }

    /**
     * Ejecuta una consulta INSERT, UPDATE o DELETE y retorna el número de filas afectadas.
     */
    private ExecutionResult executeUpdate(String sql, Object[] params, String type, String baseMessage) {
        int affected = jdbcTemplate.update(sql, params);
        String message = baseMessage + ": " + affected
                + (affected == 1 ? " fila afectada" : " filas afectadas");
        return ExecutionResult.success(message, type, sql, affected);
    }
}
