package com.automatas.sqlcompiler.application.service;

import com.automatas.sqlcompiler.domain.model.AnalysisResult;
import com.automatas.sqlcompiler.domain.model.ExecutionResult;
import com.automatas.sqlcompiler.domain.model.LexicalResult;
import com.automatas.sqlcompiler.domain.model.PhaseError;
import com.automatas.sqlcompiler.domain.model.ast.AstNode;
import com.automatas.sqlcompiler.domain.port.in.AnalyzeQueryUseCase;
import com.automatas.sqlcompiler.domain.port.out.DatabasePort;
import com.automatas.sqlcompiler.domain.port.out.OllamaPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio Orquestador del Mini-Compilador de SQL en Español.
 * Coordina la ejecución secuencial de las fases léxica, sintáctica y semántica.
 * Si la consulta es válida, la ejecuta en la base de datos PostgreSQL.
 */
@Service
public class AnalyzeQueryService implements AnalyzeQueryUseCase {

    private final QueryNormalizer queryNormalizer;
    private final NlpSegmentService nlpSegmentService;
    private final LexicalService lexicalService;
    private final SyntacticParser syntacticParser;
    private final SyntacticLlmService syntacticLlmService;
    private final SemanticService semanticService;
    private final OllamaPort ollamaPort;
    private final DatabasePort databasePort;

    public AnalyzeQueryService(
            QueryNormalizer queryNormalizer,
            NlpSegmentService nlpSegmentService,
            LexicalService lexicalService,
            SyntacticParser syntacticParser,
            SyntacticLlmService syntacticLlmService,
            SemanticService semanticService,
            OllamaPort ollamaPort,
            DatabasePort databasePort
    ) {
        this.queryNormalizer = queryNormalizer;
        this.nlpSegmentService = nlpSegmentService;
        this.lexicalService = lexicalService;
        this.syntacticParser = syntacticParser;
        this.syntacticLlmService = syntacticLlmService;
        this.semanticService = semanticService;
        this.ollamaPort = ollamaPort;
        this.databasePort = databasePort;
    }

    /**
     * Ejecuta el análisis completo de una consulta SQL en español.
     * Si la consulta es válida en todas las fases, se ejecuta en PostgreSQL.
     * 
     * @param query Consulta SQL en crudo ingresada por el usuario.
     * @return El resultado detallado de todas las fases del compilador.
     */
    @Override
    public AnalysisResult analyze(String query) {
        // 1. Normalización: Limpieza y estandarización del texto de entrada
        String normalizedQuery = queryNormalizer.normalize(query);
        List<PhaseError> phaseErrors = new ArrayList<>();

        // 2. Fase Léxica: Extracción y validación de tokens
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

        // 3. Fase Sintáctica Formal y Fase Semántica
        // Solo se ejecutan si la consulta no tiene errores léxicos previos.
        if (lexicalResult.errors().isEmpty()) {
            try {
                // Análisis Sintáctico: Construcción del árbol (AST)
                astNode = syntacticParser.parse(lexicalResult.tokens());
                astFormal = astNode.toMap();
                
                // Análisis Semántico: Validación de tablas, columnas y tipos con la Tabla de Símbolos
                semanticErrors = semanticService.analyze(astNode);
            } catch (SyntacticParser.SyntaxException ex) {
                phaseErrors.add(new PhaseError("sintactico", ex.getMessage()));
            }
        }

        // 4. Fase Sintáctica LLM (Ollama): Generación del AST con Inteligencia Artificial
        SyntacticLlmService.LlmAstResult llmResult =
                syntacticLlmService.generateAst(query, lexicalResult.tokens());

        // La consulta es completamente válida si no hay errores en ninguna fase
        boolean valid = phaseErrors.isEmpty()
                && semanticErrors.isEmpty()
                && astNode != null
                && lexicalResult.errors().isEmpty();

        // 5. Fase de Ejecución: Si la consulta es válida, ejecutarla en PostgreSQL
        ExecutionResult executionResult = null;
        if (valid && astNode != null) {
            executionResult = databasePort.execute(astNode);
        }

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
                valid,
                executionResult
        );
    }
}

