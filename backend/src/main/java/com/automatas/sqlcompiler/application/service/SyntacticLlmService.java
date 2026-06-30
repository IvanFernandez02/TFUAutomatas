package com.automatas.sqlcompiler.application.service;

import com.automatas.sqlcompiler.domain.model.Token;
import com.automatas.sqlcompiler.domain.port.out.OllamaPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyntacticLlmService {

    private final OllamaPort ollamaPort;
    private final ObjectMapper objectMapper;

    public SyntacticLlmService(OllamaPort ollamaPort, ObjectMapper objectMapper) {
        this.ollamaPort = ollamaPort;
        this.objectMapper = objectMapper;
    }

    public LlmAstResult generateAst(String query, List<Token> tokens) {
        if (!ollamaPort.isAvailable()) {
            return new LlmAstResult(null, ollamaPort.getAvailabilityMessage());
        }

        String tokenSummary = tokens.stream()
                .map(token -> token.type() + "(" + token.value() + ")")
                .collect(Collectors.joining(", "));

        String prompt = """
                Eres un analizador sintáctico SQL. Convierte la consulta en español a un árbol JSON.
                Devuelve SOLO JSON válido, sin explicaciones.

                Consulta: %s
                Tokens: %s

                Formato esperado:
                {
                  "type": "SELECT|INSERT|UPDATE|DELETE",
                  "columns": ["..."],
                  "table": "...",
                  "where": [{"column": "...", "operator": "...", "value": "..."}]
                }
                """.formatted(query, tokenSummary);

        try {
            String response = ollamaPort.generate(prompt);
            Map<String, Object> ast = objectMapper.readValue(response, new TypeReference<>() {
            });
            return new LlmAstResult(ast, "Árbol generado con Ollama");
        } catch (Exception ex) {
            return new LlmAstResult(null, "Error al consultar Ollama: " + ex.getMessage());
        }
    }

    public record LlmAstResult(Map<String, Object> ast, String message) {
    }
}
