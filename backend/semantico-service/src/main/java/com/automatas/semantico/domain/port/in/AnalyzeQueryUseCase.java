package com.automatas.semantico.domain.port.in;

import com.automatas.shared.domain.model.AnalysisResult;

/**
 * Puerto de entrada: caso de uso principal del orquestador.
 */
public interface AnalyzeQueryUseCase {

    AnalysisResult analyze(String query);
}
