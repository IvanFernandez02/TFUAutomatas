package com.automatas.semantico.infrastructure.adapter.out.http;

import com.automatas.shared.domain.model.LexicalError;
import com.automatas.shared.domain.model.LexicalResult;
import com.automatas.shared.domain.model.Token;
import com.automatas.semantico.domain.port.out.LexicalPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementacion Bridge: llama al microservicio Lexico via HTTP REST.
 */
@Component
public class HttpLexicalAdapter implements LexicalPort {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String lexicoUrl;

    public HttpLexicalAdapter(
            ObjectMapper objectMapper,
            @Value("${lexico.service.url}") String lexicoUrl
    ) {
        this.objectMapper = objectMapper;
        this.lexicoUrl = lexicoUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public LexicalResult analyze(String source) {
        try {
            Map<String, String> body = Map.of("source", source);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(lexicoUrl + "/api/lexical"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Error del servicio lexico: HTTP " + response.statusCode());
            }

            JsonNode json = objectMapper.readTree(response.body());

            List<Token> tokens = new ArrayList<>();
            JsonNode tokensNode = json.get("tokens");
            if (tokensNode != null && tokensNode.isArray()) {
                for (JsonNode t : tokensNode) {
                    tokens.add(new Token(
                            t.get("type").asText(),
                            t.get("value").asText(),
                            t.get("line").asInt(),
                            t.get("column").asInt()
                    ));
                }
            }

            List<LexicalError> errors = new ArrayList<>();
            JsonNode errorsNode = json.get("errors");
            if (errorsNode != null && errorsNode.isArray()) {
                for (JsonNode e : errorsNode) {
                    errors.add(new LexicalError(
                            e.get("token").asText(),
                            e.get("position").asInt()
                    ));
                }
            }

            return new LexicalResult(tokens, errors);
        } catch (Exception ex) {
            throw new RuntimeException("Error al conectar con el servicio lexico: " + ex.getMessage(), ex);
        }
    }
}
