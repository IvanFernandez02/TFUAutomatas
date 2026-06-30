package com.automatas.sqlcompiler.infrastructure.adapter.in.web;

import com.automatas.sqlcompiler.domain.model.AnalysisResult;
import com.automatas.sqlcompiler.domain.model.Token;
import com.automatas.sqlcompiler.domain.port.in.AnalyzeQueryUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AnalyzeController.class, SystemController.class})
class AnalyzeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyzeQueryUseCase analyzeQueryUseCase;

    @MockBean
    private com.automatas.sqlcompiler.domain.port.out.OllamaPort ollamaPort;

    @MockBean
    private com.automatas.sqlcompiler.domain.port.out.SchemaRepositoryPort schemaRepositoryPort;

    @Test
    void analyzeReturnsSemanticErrorPayload() throws Exception {
        when(analyzeQueryUseCase.analyze(anyString())).thenReturn(new AnalysisResult(
                "SELECCIONAR salario DESDE usuarios",
                "SELECCIONAR SALARIO DESDE USUARIOS",
                List.of(new Token("SELECCIONAR", "SELECCIONAR", 1, 0)),
                List.of(),
                Map.of("type", "SELECT"),
                null,
                false,
                "Ollama no disponible",
                Map.of(),
                List.of("Error semántico: la columna 'salario' no existe en la tabla 'usuarios'"),
                List.of(),
                false
        ));

        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"SELECCIONAR salario DESDE usuarios\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.semantic_errors[0]").value("Error semántico: la columna 'salario' no existe en la tabla 'usuarios'"));
    }

    @Test
    void healthEndpointWorks() throws Exception {
        when(ollamaPort.isAvailable()).thenReturn(true);
        when(ollamaPort.getAvailabilityMessage()).thenReturn("Ollama disponible");

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.ollama_available").value(true));
    }
}
