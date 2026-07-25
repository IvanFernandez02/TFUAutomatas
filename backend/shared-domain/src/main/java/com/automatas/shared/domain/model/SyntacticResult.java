package com.automatas.shared.domain.model;

import java.util.List;
import java.util.Map;

public record SyntacticResult(
        Map<String, Object> ast,
        Map<String, Object> astLlm,
        String llmMessage,
        List<SyntacticError> errors
) {
}
