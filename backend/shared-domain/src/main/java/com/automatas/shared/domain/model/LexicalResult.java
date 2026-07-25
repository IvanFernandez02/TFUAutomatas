package com.automatas.shared.domain.model;

import java.util.List;

public record LexicalResult(
        List<Token> tokens,
        List<LexicalError> errors
) {
}
