package com.automatas.sqlcompiler.domain.port.out;

import com.automatas.sqlcompiler.domain.model.ExecutionResult;
import com.automatas.sqlcompiler.domain.model.ast.AstNode;

import java.util.List;
import java.util.Map;

/**
 * Puerto de salida para la ejecución de consultas SQL en la base de datos.
 * Siguiendo la arquitectura hexagonal, esta interfaz desacopla el dominio
 * de la implementación concreta de PostgreSQL.
 */
public interface DatabasePort {

    /**
     * Ejecuta una consulta SQL representada por un nodo AST en la base de datos.
     *
     * @param node Nodo del Árbol Sintáctico Abstracto ya validado.
     * @return El resultado de la ejecución.
     */
    ExecutionResult execute(AstNode node);

    /**
     * Obtiene todos los datos de una tabla.
     *
     * @param tableName Nombre de la tabla.
     * @return Lista de filas como mapas columna→valor.
     */
    List<Map<String, Object>> queryTable(String tableName);
}
