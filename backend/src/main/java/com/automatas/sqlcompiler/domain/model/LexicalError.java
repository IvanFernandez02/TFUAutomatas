package com.automatas.sqlcompiler.domain.model;

public record LexicalError(String token, int position) {
}
