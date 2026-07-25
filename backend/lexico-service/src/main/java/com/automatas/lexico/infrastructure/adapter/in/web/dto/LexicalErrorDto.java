package com.automatas.lexico.infrastructure.adapter.in.web.dto;

import com.automatas.shared.domain.model.LexicalError;

public record LexicalErrorDto(String token, int position) {
    public static LexicalErrorDto from(LexicalError error) {
        return new LexicalErrorDto(error.token(), error.position());
    }
}
