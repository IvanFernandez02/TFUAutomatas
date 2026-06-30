package com.automatas.sqlcompiler.domain.model;

import java.util.List;
import java.util.Map;

public record LexicalResult(
        List<Token> tokens,
        List<LexicalError> errors,
        Map<String, Object> explanation
) {
}
