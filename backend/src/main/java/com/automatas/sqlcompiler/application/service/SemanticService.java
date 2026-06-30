package com.automatas.sqlcompiler.application.service;

import com.automatas.sqlcompiler.domain.model.ast.AstNode;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.Assignment;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.Condition;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.DeleteNode;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.InsertNode;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.SelectNode;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.UpdateNode;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.Value;
import com.automatas.sqlcompiler.domain.port.out.SchemaRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SemanticService {

    private final SchemaRepositoryPort schemaRepository;

    public SemanticService(SchemaRepositoryPort schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    public List<String> analyze(AstNode node) {
        List<String> errors = new ArrayList<>();
        if (node == null) {
            errors.add("No se pudo analizar semánticamente: AST vacío");
            return errors;
        }

        switch (node) {
            case SelectNode select -> analyzeSelect(select, errors);
            case InsertNode insert -> analyzeInsert(insert, errors);
            case UpdateNode update -> analyzeUpdate(update, errors);
            case DeleteNode delete -> analyzeDelete(delete, errors);
            default -> errors.add("Tipo de nodo no soportado");
        }

        return errors;
    }

    private void analyzeSelect(SelectNode node, List<String> errors) {
        String table = node.table().toLowerCase();
        if (!schemaRepository.tableExists(table)) {
            errors.add("Error semántico: la tabla '" + node.table() + "' no existe");
            return;
        }

        if (node.columns().equals(List.of("*"))) {
            validateConditions(table, node.where(), node.table(), errors);
            return;
        }

        for (String column : node.columns()) {
            if (!schemaRepository.columnExists(table, column)) {
                errors.add("Error semántico: la columna '" + column + "' no existe en la tabla '" + node.table() + "'");
            }
        }

        validateConditions(table, node.where(), node.table(), errors);
    }

    private void analyzeInsert(InsertNode node, List<String> errors) {
        String table = node.table().toLowerCase();
        if (!schemaRepository.tableExists(table)) {
            errors.add("Error semántico: la tabla '" + node.table() + "' no existe");
            return;
        }

        if (node.columns().size() != node.values().size()) {
            errors.add("Error semántico: el número de columnas no coincide con el número de valores");
        }

        for (String column : node.columns()) {
            if (!schemaRepository.columnExists(table, column)) {
                errors.add("Error semántico: la columna '" + column + "' no existe en la tabla '" + node.table() + "'");
            }
        }

        for (int i = 0; i < node.columns().size() && i < node.values().size(); i++) {
            validateValueType(table, node.columns().get(i), node.values().get(i), node.table(), errors);
        }
    }

    private void analyzeUpdate(UpdateNode node, List<String> errors) {
        String table = node.table().toLowerCase();
        if (!schemaRepository.tableExists(table)) {
            errors.add("Error semántico: la tabla '" + node.table() + "' no existe");
            return;
        }

        for (Assignment assignment : node.assignments()) {
            if (!schemaRepository.columnExists(table, assignment.column())) {
                errors.add("Error semántico: la columna '" + assignment.column()
                        + "' no existe en la tabla '" + node.table() + "'");
            } else {
                validateValueType(table, assignment.column(), assignment.value(), node.table(), errors);
            }
        }

        validateConditions(table, node.where(), node.table(), errors);
    }

    private void analyzeDelete(DeleteNode node, List<String> errors) {
        String table = node.table().toLowerCase();
        if (!schemaRepository.tableExists(table)) {
            errors.add("Error semántico: la tabla '" + node.table() + "' no existe");
            return;
        }

        validateConditions(table, node.where(), node.table(), errors);
    }

    private void validateConditions(String table, List<Condition> conditions, String tableName, List<String> errors) {
        for (Condition condition : conditions) {
            if (!schemaRepository.columnExists(table, condition.column())) {
                errors.add("Error semántico: la columna '" + condition.column()
                        + "' no existe en la tabla '" + tableName + "'");
            } else {
                validateValueType(table, condition.column(), condition.value(), tableName, errors);
            }
        }
    }

    private void validateValueType(String table, String column, Value value, String tableName, List<String> errors) {
        schemaRepository.getColumnType(table, column).ifPresent(columnType -> {
            if ("number".equals(value.kind()) && !NUMERIC_TYPES.contains(columnType)) {
                errors.add("Error semántico: se esperaba un valor de tipo '" + columnType
                        + "' para la columna '" + column + "' de '" + tableName + "', se recibió un número");
            }
            if ("string".equals(value.kind()) && !"texto".equals(columnType)) {
                errors.add("Error semántico: se esperaba un valor de tipo '" + columnType
                        + "' para la columna '" + column + "' de '" + tableName + "', se recibió texto");
            }
        });
    }

    private static final java.util.Set<String> NUMERIC_TYPES = java.util.Set.of("entero", "decimal");
}
