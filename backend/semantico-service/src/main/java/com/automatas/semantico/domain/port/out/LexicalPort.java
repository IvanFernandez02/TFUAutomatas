package com.automatas.semantico.domain.port.out;

import com.automatas.shared.domain.model.LexicalResult;

/**
 * Puerto de salida: abstraccion para el analisis lexico.
 * Patron Bridge: define el contrato que los adaptadores HTTP deben implementar.
 */
public interface LexicalPort {

    LexicalResult analyze(String source);
}
