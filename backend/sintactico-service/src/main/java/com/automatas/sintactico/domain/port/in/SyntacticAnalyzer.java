package com.automatas.sintactico.domain.port.in;

import com.automatas.shared.domain.model.SyntacticResult;

import java.util.List;
import java.util.Map;

/**
 * Abstraccion del analizador sintactico (Patron Bridge).
 * Define el contrato para cualquier implementacion de parsing.
 */
public interface SyntacticAnalyzer {

    /**
     * Analiza sintacticamente una lista de tokens.
     *
     * @param originalQuery Consulta original (para LLM).
     * @param tokens        Tokens extraidos por el analizador lexico.
     * @return Resultado con el AST formal, AST LLM y errores.
     */
    SyntacticResult analyze(String originalQuery, List<Map<String, Object>> tokens);
}
