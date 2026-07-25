package com.automatas.semantico.infrastructure.adapter.in.web.dto;

import com.automatas.shared.domain.model.PhaseError;

public record PhaseErrorDto(String phase, String message) {
    public static PhaseErrorDto from(PhaseError error) {
        return new PhaseErrorDto(error.phase(), error.message());
    }
}
