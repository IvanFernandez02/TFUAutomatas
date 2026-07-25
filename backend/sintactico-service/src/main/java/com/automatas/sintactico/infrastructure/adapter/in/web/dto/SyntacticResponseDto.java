package com.automatas.sintactico.infrastructure.adapter.in.web.dto;

import com.automatas.shared.domain.model.SyntacticError;

import java.util.List;
import java.util.Map;

public record SyntacticResponseDto(
        Map<String, Object> ast,
        Map<String, Object> astLlm,
        String llmMessage,
        List<SyntacticErrorDto> errors
) {
    public static SyntacticResponseDto from(com.automatas.shared.domain.model.SyntacticResult result) {
        return new SyntacticResponseDto(
                result.ast(),
                result.astLlm(),
                result.llmMessage(),
                result.errors().stream().map(e -> new SyntacticErrorDto(e.phase(), e.message())).toList()
        );
    }
}
