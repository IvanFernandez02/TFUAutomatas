package com.automatas.sqlcompiler.domain.model;

import java.util.List;
import java.util.Map;

public record AnalysisResult(
        String query,
        String normalizedQuery,
        List<Token> tokens,
        List<NlpSegment> nlpSegments,
        Map<String, Object> astFormal,
        Map<String, Object> astLlm,
        boolean astLlmAvailable,
        String astLlmMessage,
        Map<String, Object> lexicalExplanation,
        List<String> semanticErrors,
        List<PhaseError> phaseErrors,
        boolean valid
) {
}
