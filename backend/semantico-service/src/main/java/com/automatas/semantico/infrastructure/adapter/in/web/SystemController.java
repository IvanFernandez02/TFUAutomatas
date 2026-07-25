package com.automatas.semantico.infrastructure.adapter.in.web;

import com.automatas.shared.domain.port.out.DatabasePort;
import com.automatas.shared.domain.port.out.OllamaPort;
import com.automatas.shared.domain.port.out.SchemaRepositoryPort;
import com.automatas.semantico.infrastructure.adapter.in.web.dto.HealthResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemController {

    private final OllamaPort ollamaPort;
    private final SchemaRepositoryPort schemaRepositoryPort;
    private final DatabasePort databasePort;

    public SystemController(OllamaPort ollamaPort,
                            SchemaRepositoryPort schemaRepositoryPort,
                            DatabasePort databasePort) {
        this.ollamaPort = ollamaPort;
        this.schemaRepositoryPort = schemaRepositoryPort;
        this.databasePort = databasePort;
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

    @GetMapping("/tables/{name}/data")
    public List<Map<String, Object>> tableData(@PathVariable String name) {
        return databasePort.queryTable(name);
    }
}
