package com.automatas.sqlcompiler.infrastructure.adapter.in.web.dto;

import com.automatas.sqlcompiler.domain.model.AnalysisResult;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record AnalyzeResponseDto(
        String query,
        @JsonProperty("normalized_query") String normalizedQuery,
        List<TokenDto> tokens,
        @JsonProperty("nlp_segments") List<NlpSegmentDto> nlpSegments,
        @JsonProperty("ast_ply") Map<String, Object> astPly,
        @JsonProperty("ast_llm") Map<String, Object> astLlm,
        @JsonProperty("ast_llm_available") boolean astLlmAvailable,
        @JsonProperty("ast_llm_message") String astLlmMessage,
        @JsonProperty("lexical_explanation") Map<String, Object> lexicalExplanation,
        @JsonProperty("semantic_errors") List<String> semanticErrors,
        @JsonProperty("phase_errors") List<PhaseErrorDto> phaseErrors,
        boolean valid,
        @JsonProperty("execution_result") ExecutionResultDto executionResult
) {
    public static AnalyzeResponseDto from(AnalysisResult result) {
        return new AnalyzeResponseDto(
                result.query(),
                result.normalizedQuery(),
                result.tokens().stream().map(TokenDto::from).toList(),
                result.nlpSegments().stream().map(NlpSegmentDto::from).toList(),
                result.astFormal(),
                result.astLlm(),
                result.astLlmAvailable(),
                result.astLlmMessage(),
                result.lexicalExplanation(),
                result.semanticErrors(),
                result.phaseErrors().stream().map(PhaseErrorDto::from).toList(),
                result.valid(),
                ExecutionResultDto.from(result.executionResult())
        );
    }
}
