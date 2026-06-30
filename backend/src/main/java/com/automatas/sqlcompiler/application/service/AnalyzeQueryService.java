package com.automatas.sqlcompiler.application.service;

import com.automatas.sqlcompiler.domain.model.AnalysisResult;
import com.automatas.sqlcompiler.domain.model.LexicalResult;
import com.automatas.sqlcompiler.domain.model.PhaseError;
import com.automatas.sqlcompiler.domain.model.ast.AstNode;
import com.automatas.sqlcompiler.domain.port.in.AnalyzeQueryUseCase;
import com.automatas.sqlcompiler.domain.port.out.OllamaPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AnalyzeQueryService implements AnalyzeQueryUseCase {

    private final QueryNormalizer queryNormalizer;
    private final NlpSegmentService nlpSegmentService;
    private final LexicalService lexicalService;
    private final SyntacticParser syntacticParser;
    private final SyntacticLlmService syntacticLlmService;
    private final SemanticService semanticService;
    private final OllamaPort ollamaPort;

    public AnalyzeQueryService(
            QueryNormalizer queryNormalizer,
            NlpSegmentService nlpSegmentService,
            LexicalService lexicalService,
            SyntacticParser syntacticParser,
            SyntacticLlmService syntacticLlmService,
            SemanticService semanticService,
            OllamaPort ollamaPort
    ) {
        this.queryNormalizer = queryNormalizer;
        this.nlpSegmentService = nlpSegmentService;
        this.lexicalService = lexicalService;
        this.syntacticParser = syntacticParser;
        this.syntacticLlmService = syntacticLlmService;
        this.semanticService = semanticService;
        this.ollamaPort = ollamaPort;
    }

    @Override
    public AnalysisResult analyze(String query) {
        String normalizedQuery = queryNormalizer.normalize(query);
        List<PhaseError> phaseErrors = new ArrayList<>();

        LexicalResult lexicalResult = lexicalService.analyze(normalizedQuery);
        if (!lexicalResult.errors().isEmpty()) {
            phaseErrors.add(new PhaseError(
                    "lexico",
                    "Se detectaron tokens inválidos en la consulta"
            ));
        }

        Map<String, Object> astFormal = null;
        AstNode astNode = null;
        List<String> semanticErrors = List.of();

        if (lexicalResult.errors().isEmpty()) {
            try {
                astNode = syntacticParser.parse(lexicalResult.tokens());
                astFormal = astNode.toMap();
                semanticErrors = semanticService.analyze(astNode);
            } catch (SyntacticParser.SyntaxException ex) {
                phaseErrors.add(new PhaseError("sintactico", ex.getMessage()));
            }
        }

        SyntacticLlmService.LlmAstResult llmResult =
                syntacticLlmService.generateAst(query, lexicalResult.tokens());

        boolean valid = phaseErrors.isEmpty()
                && semanticErrors.isEmpty()
                && astNode != null
                && lexicalResult.errors().isEmpty();

        return new AnalysisResult(
                query,
                normalizedQuery,
                lexicalResult.tokens(),
                nlpSegmentService.segment(normalizedQuery),
                astFormal,
                llmResult.ast(),
                ollamaPort.isAvailable(),
                llmResult.message(),
                lexicalResult.explanation(),
                semanticErrors,
                phaseErrors,
                valid
        );
    }
}
