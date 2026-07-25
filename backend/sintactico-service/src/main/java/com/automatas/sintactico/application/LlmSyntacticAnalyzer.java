package com.automatas.sintactico.application;

import com.automatas.shared.domain.model.Token;
import com.automatas.shared.domain.port.out.OllamaPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementacion concreta: Analisis sintactico asistido por LLM (Ollama).
 * Patron Bridge: implementacion alternativa del analisis sintactico.
 */
@Component
public class LlmSyntacticAnalyzer {

    private final OllamaPort ollamaPort;
    private final ObjectMapper objectMapper;

    public LlmSyntacticAnalyzer(OllamaPort ollamaPort, ObjectMapper objectMapper) {
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
                Eres un analizador sintactico SQL. Convierte la consulta en español a un arbol JSON.
                Devuelve SOLO JSON valido, sin explicaciones.

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
            return new LlmAstResult(ast, "Arbol generado con Ollama");
        } catch (Exception ex) {
            return new LlmAstResult(null, "Error al consultar Ollama: " + ex.getMessage());
        }
    }

    public record LlmAstResult(Map<String, Object> ast, String message) {
    }
}
