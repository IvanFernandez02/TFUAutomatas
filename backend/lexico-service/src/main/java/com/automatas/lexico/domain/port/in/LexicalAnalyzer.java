package com.automatas.lexico.domain.port.in;

import com.automatas.shared.domain.model.LexicalResult;

/**
 * Abstraccion del analizador lexico (Patron Bridge).
 * Define el contrato para cualquier implementacion de tokenizacion.
 */
public interface LexicalAnalyzer {

    /**
     * Analiza lexicamente una consulta SQL normalizada.
     *
     * @param source Texto SQL normalizado (en mayusculas, sin espacios extra).
     * @return Resultado con tokens extraidos y errores encontrados.
     */
    LexicalResult analyze(String source);
}
