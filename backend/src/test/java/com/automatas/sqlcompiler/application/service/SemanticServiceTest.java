package com.automatas.sqlcompiler.application.service;

import com.automatas.sqlcompiler.domain.model.ast.AstNode.SelectNode;
import com.automatas.sqlcompiler.domain.port.out.SchemaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticServiceTest {

    private SemanticService semanticService;

    @BeforeEach
    void setUp() {
        semanticService = new SemanticService(new InMemorySchemaRepository());
    }

    @Test
    void detectsMissingColumn() {
        SelectNode node = new SelectNode(List.of("SALARIO"), "USUARIOS", List.of());
        List<String> errors = semanticService.analyze(node);
        assertFalse(errors.isEmpty());
        assertTrue(errors.getFirst().toLowerCase().contains("salario"));
    }

    @Test
    void acceptsValidSelect() {
        SelectNode node = new SelectNode(List.of("NOMBRE", "EDAD"), "USUARIOS", List.of());
        assertTrue(semanticService.analyze(node).isEmpty());
    }

    private static class InMemorySchemaRepository implements SchemaRepositoryPort {
        private final Map<String, Map<String, Object>> schema = Map.of(
                "usuarios", Map.of("columnas", Map.of(
                        "id", "entero",
                        "nombre", "texto",
                        "edad", "entero",
                        "email", "texto"
                ))
        );

        @Override
        public Map<String, Map<String, Object>> loadSchema() {
            return schema;
        }

        @Override
        public boolean tableExists(String table) {
            return schema.containsKey(table.toLowerCase());
        }

        @Override
        public boolean columnExists(String table, String column) {
            Map<String, Object> tableData = schema.get(table.toLowerCase());
            if (tableData == null) {
                return false;
            }
            Map<String, String> columns = (Map<String, String>) tableData.get("columnas");
            return columns.containsKey(column.toLowerCase());
        }

        @Override
        public Optional<String> getColumnType(String table, String column) {
            Map<String, Object> tableData = schema.get(table.toLowerCase());
            Map<String, String> columns = (Map<String, String>) tableData.get("columnas");
            return Optional.ofNullable(columns.get(column.toLowerCase()));
        }

        @Override
        public List<String> getColumns(String table) {
            Map<String, Object> tableData = schema.get(table.toLowerCase());
            Map<String, String> columns = (Map<String, String>) tableData.get("columnas");
            return List.copyOf(columns.keySet());
        }
    }
}
