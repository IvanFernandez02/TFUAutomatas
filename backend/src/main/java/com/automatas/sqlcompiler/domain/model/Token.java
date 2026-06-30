package com.automatas.sqlcompiler.domain.model;

public record Token(String type, String value, int line, int column) {
}
