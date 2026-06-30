package com.automatas.sqlcompiler.infrastructure.adapter.in.web;

import com.automatas.sqlcompiler.domain.port.out.OllamaPort;
import com.automatas.sqlcompiler.domain.port.out.SchemaRepositoryPort;
import com.automatas.sqlcompiler.infrastructure.adapter.in.web.dto.HealthResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemController {

    private final OllamaPort ollamaPort;
    private final SchemaRepositoryPort schemaRepositoryPort;

    public SystemController(OllamaPort ollamaPort, SchemaRepositoryPort schemaRepositoryPort) {
        this.ollamaPort = ollamaPort;
        this.schemaRepositoryPort = schemaRepositoryPort;
    }

    @GetMapping("/health")
    public HealthResponseDto health() {
        return new HealthResponseDto(
                "ok",
                ollamaPort.isAvailable(),
                ollamaPort.getAvailabilityMessage()
        );
    }

    @GetMapping("/schema")
    public Map<String, Map<String, Object>> schema() {
        return schemaRepositoryPort.loadSchema();
    }
}
