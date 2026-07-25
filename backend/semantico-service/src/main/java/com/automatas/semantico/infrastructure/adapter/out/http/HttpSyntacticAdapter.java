package com.automatas.semantico.infrastructure.adapter.out.http;

import com.automatas.shared.domain.model.SyntacticError;
import com.automatas.shared.domain.model.SyntacticResult;
import com.automatas.semantico.domain.port.out.SyntacticPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Implementacion Bridge: llama al microservicio Sintactico via HTTP REST.
 */
@Component
public class HttpSyntacticAdapter implements SyntacticPort {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String sintacticoUrl;

    public HttpSyntacticAdapter(
            ObjectMapper objectMapper,
            @Value("${sintactico.service.url}") String sintacticoUrl
    ) {
        this.objectMapper = objectMapper;
        this.sintacticoUrl = sintacticoUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public SyntacticResult analyze(String originalQuery, List<Map<String, Object>> tokens) {
        try {
            Map<String, Object> body = Map.of(
                    "query", originalQuery,
                    "tokens", tokens
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sintacticoUrl + "/api/syntactic"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Error del servicio sintactico: HTTP " + response.statusCode());
            }

            JsonNode json = objectMapper.readTree(response.body());

            Map<String, Object> ast = null;
            JsonNode astNode = json.get("ast");
            if (astNode != null && !astNode.isNull()) {
                ast = objectMapper.convertValue(astNode, new TypeReference<>() {});
            }

            Map<String, Object> astLlm = null;
            JsonNode astLlmNode = json.get("astLlm");
            if (astLlmNode != null && !astLlmNode.isNull()) {
                astLlm = objectMapper.convertValue(astLlmNode, new TypeReference<>() {});
            }

            String llmMessage = json.has("llmMessage") ? json.get("llmMessage").asText(null) : null;

            List<SyntacticError> errors = List.of();
            JsonNode errorsNode = json.get("errors");
            if (errorsNode != null && errorsNode.isArray()) {
                errors = objectMapper.convertValue(errorsNode, new TypeReference<>() {});
            }

            return new SyntacticResult(ast, astLlm, llmMessage, errors);
        } catch (Exception ex) {
            throw new RuntimeException("Error al conectar con el servicio sintactico: " + ex.getMessage(), ex);
        }
    }
}
