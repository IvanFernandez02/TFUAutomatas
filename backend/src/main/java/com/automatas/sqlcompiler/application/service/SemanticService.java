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

/**
 * Analizador Semántico del Mini-Compilador SQL.
 * Valida la coherencia de la consulta contra el esquema de datos (Tabla de Símbolos),
 * incluyendo la existencia de tablas, columnas y la correspondencia de tipos.
 */
@Service
public class SemanticService {

    private final SchemaRepositoryPort schemaRepository;

    public SemanticService(SchemaRepositoryPort schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    /**
     * Analiza semánticamente un nodo del árbol sintáctico (AST).
     * 
     * @param node El nodo raíz del AST generado por el parser.
     * @return Una lista de mensajes de error semánticos encontrados. Lista vacía si es correcto.
     */
    public List<String> analyze(AstNode node) {
        List<String> errors = new ArrayList<>();
        if (node == null) {
            errors.add("No se pudo analizar semánticamente: AST vacío");
            return errors;
        }

        // Redirección según el tipo de operación SQL detectado en el AST
        switch (node) {
            case SelectNode select -> analyzeSelect(select, errors);
            case InsertNode insert -> analyzeInsert(insert, errors);
            case UpdateNode update -> analyzeUpdate(update, errors);
            case DeleteNode delete -> analyzeDelete(delete, errors);
            default -> errors.add("Tipo de nodo no soportado");
        }

        return errors;
    }

    /**
     * Valida sentencias de tipo SELECCIONAR.
     * Verifica que la tabla exista y que todas las columnas seleccionadas y condicionadas existan en ella.
     */
    private void analyzeSelect(SelectNode node, List<String> errors) {
        String table = node.table().toLowerCase();
        
        // Regla 1: Validar existencia de la tabla en el esquema
        if (!schemaRepository.tableExists(table)) {
            errors.add("Error semántico: la tabla '" + node.table() + "' no existe");
            return;
        }

        // Si se seleccionó todo (*), omitimos verificar columnas específicas de la cláusula
        if (node.columns().equals(List.of("*"))) {
            validateConditions(table, node.where(), node.table(), errors);
            return;
        }

        // Regla 2: Validar que cada columna solicitada exista en la tabla indicada
        for (String column : node.columns()) {
            if (!schemaRepository.columnExists(table, column)) {
                errors.add("Error semántico: la columna '" + column + "' no existe en la tabla '" + node.table() + "'");
            }
        }

        // Regla 3: Validar columnas y tipos en la cláusula CUANDO / DONDE
        validateConditions(table, node.where(), node.table(), errors);
    }

    /**
     * Valida sentencias de tipo INSERTAR.
     * Verifica la correspondencia de tamaño entre columnas y valores, y la compatibilidad de tipos.
     */
    private void analyzeInsert(InsertNode node, List<String> errors) {
        String table = node.table().toLowerCase();
        if (!schemaRepository.tableExists(table)) {
            errors.add("Error semántico: la tabla '" + node.table() + "' no existe");
            return;
        }

        // Regla: Número de columnas debe ser igual al número de valores
        if (node.columns().size() != node.values().size()) {
            errors.add("Error semántico: el número de columnas no coincide con el número de valores");
        }

        // Regla: Validar existencia de las columnas en la tabla
        for (String column : node.columns()) {
            if (!schemaRepository.columnExists(table, column)) {
                errors.add("Error semántico: la columna '" + column + "' no existe en la tabla '" + node.table() + "'");
            }
        }

        // Regla: Validación de tipo de datos (Chequeo de Tipos) para cada columna-valor
        for (int i = 0; i < node.columns().size() && i < node.values().size(); i++) {
            validateValueType(table, node.columns().get(i), node.values().get(i), node.table(), errors);
        }
    }

    /**
     * Valida sentencias de tipo MODIFICAR.
     */
    private void analyzeUpdate(UpdateNode node, List<String> errors) {
        String table = node.table().toLowerCase();
        if (!schemaRepository.tableExists(table)) {
            errors.add("Error semántico: la tabla '" + node.table() + "' no existe");
            return;
        }

        // Validar columnas y tipos en las asignaciones de ESTABLECER (SET)
        for (Assignment assignment : node.assignments()) {
            if (!schemaRepository.columnExists(table, assignment.column())) {
                errors.add("Error semántico: la columna '" + assignment.column()
                        + "' no existe en la tabla '" + node.table() + "'");
            } else {
                validateValueType(table, assignment.column(), assignment.value(), node.table(), errors);
            }
        }

        // Validar condiciones del CUANDO
        validateConditions(table, node.where(), node.table(), errors);
    }

    /**
     * Valida sentencias de tipo ELIMINAR.
     */
    private void analyzeDelete(DeleteNode node, List<String> errors) {
        String table = node.table().toLowerCase();
        if (!schemaRepository.tableExists(table)) {
            errors.add("Error semántico: la tabla '" + node.table() + "' no existe");
            return;
        }

        validateConditions(table, node.where(), node.table(), errors);
    }

    /**
     * Valida que las columnas referenciadas en las condiciones de la consulta existan y tengan el tipo de dato correcto.
     */
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

    /**
     * Compara los tipos de datos declarados en la tabla de símbolos (schema.json) contra los valores del AST.
     */
    private void validateValueType(String table, String column, Value value, String tableName, List<String> errors) {
        schemaRepository.getColumnType(table, column).ifPresent(columnType -> {
            // Verificar si se introdujo número en una columna que no es numérica
            if ("number".equals(value.kind()) && !NUMERIC_TYPES.contains(columnType)) {
                errors.add("Error semántico: se esperaba un valor de tipo '" + columnType
                        + "' para la columna '" + column + "' de '" + tableName + "', se recibió un número");
            }
            // Verificar si se introdujo texto en una columna que no es de texto
            if ("string".equals(value.kind()) && !"texto".equals(columnType)) {
                errors.add("Error semántico: se esperaba un valor de tipo '" + columnType
                        + "' para la columna '" + column + "' de '" + tableName + "', se recibió texto");
            }
        });
    }

    private static final java.util.Set<String> NUMERIC_TYPES = java.util.Set.of("entero", "decimal");
}

