package com.automatas.semantico.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HealthResponseDto(
        String status,
        @JsonProperty("ollama_available") boolean ollamaAvailable,
        @JsonProperty("ollama_message") String ollamaMessage
) {
}
