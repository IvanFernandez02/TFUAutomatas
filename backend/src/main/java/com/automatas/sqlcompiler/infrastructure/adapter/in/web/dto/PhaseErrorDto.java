package com.automatas.sqlcompiler.infrastructure.adapter.in.web.dto;

import com.automatas.sqlcompiler.domain.model.PhaseError;

public record PhaseErrorDto(String phase, String message) {
    public static PhaseErrorDto from(PhaseError error) {
        return new PhaseErrorDto(error.phase(), error.message());
    }
}
