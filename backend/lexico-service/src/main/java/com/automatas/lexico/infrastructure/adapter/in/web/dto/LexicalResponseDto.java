package com.automatas.lexico.infrastructure.adapter.in.web.dto;

import com.automatas.shared.domain.model.LexicalResult;

import java.util.List;

public record LexicalResponseDto(
        List<TokenDto> tokens,
        List<LexicalErrorDto> errors
) {
    public static LexicalResponseDto from(LexicalResult result) {
        return new LexicalResponseDto(
                result.tokens().stream().map(TokenDto::from).toList(),
                result.errors().stream().map(LexicalErrorDto::from).toList()
        );
    }
}