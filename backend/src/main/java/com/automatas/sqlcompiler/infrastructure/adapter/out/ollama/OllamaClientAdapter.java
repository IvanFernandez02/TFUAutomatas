package com.automatas.sqlcompiler.infrastructure.adapter.out.ollama;

import com.automatas.sqlcompiler.domain.port.out.OllamaPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
public class OllamaClientAdapter implements OllamaPort {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String generateUrl;
    private final String tagsUrl;
    private final String model;

    public OllamaClientAdapter(
            ObjectMapper objectMapper,
            @Value("${ollama.host}") String generateUrl,
            @Value("${ollama.tags-url}") String tagsUrl,
            @Value("${ollama.model}") String model
    ) {
        this.objectMapper = objectMapper;
        this.generateUrl = generateUrl;
        this.tagsUrl = tagsUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tagsUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(3))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String getAvailabilityMessage() {
        return isAvailable() ? "Ollama disponible" : "Ollama no disponible";
    }

    @Override
    public String generate(String prompt) {
        try {
            Map<String, Object> payload = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(generateUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Ollama respondió con estado " + response.statusCode());
            }

            JsonNode body = objectMapper.readTree(response.body());
            JsonNode content = body.get("response");
            if (content == null || content.asText().isBlank()) {
                throw new IllegalStateException("Ollama no devolvió contenido");
            }
            return content.asText().trim();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al consultar Ollama: " + ex.getMessage(), ex);
        }
    }
}
