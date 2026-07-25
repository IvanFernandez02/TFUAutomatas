package com.automatas.semantico.domain.port.out;

import com.automatas.shared.domain.model.SyntacticResult;

import java.util.List;
import java.util.Map;

/**
 * Puerto de salida: abstraccion para el analisis sintactico.
 * Patron Bridge: define el contrato que los adaptadores HTTP deben implementar.
 */
public interface SyntacticPort {

    SyntacticResult analyze(String originalQuery, List<Map<String, Object>> tokens);
}
