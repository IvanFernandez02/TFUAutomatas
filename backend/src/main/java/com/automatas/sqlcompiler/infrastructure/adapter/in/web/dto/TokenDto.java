package com.automatas.sqlcompiler.infrastructure.adapter.in.web.dto;

import com.automatas.sqlcompiler.domain.model.Token;

public record TokenDto(String type, String value, int line, int column) {
    public static TokenDto from(Token token) {
        return new TokenDto(token.type(), token.value(), token.line(), token.column());
    }
}
