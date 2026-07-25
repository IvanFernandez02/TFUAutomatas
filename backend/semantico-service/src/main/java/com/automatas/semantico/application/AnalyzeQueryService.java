package com.automatas.semantico.application;

import com.automatas.shared.domain.model.AnalysisResult;
import com.automatas.shared.domain.model.ExecutionResult;
import com.automatas.shared.domain.model.LexicalResult;
import com.automatas.shared.domain.model.PhaseError;
import com.automatas.shared.domain.model.SyntacticError;
import com.automatas.shared.domain.model.SyntacticResult;
import com.automatas.shared.domain.model.ast.AstNode;
import com.automatas.semantico.domain.port.in.AnalyzeQueryUseCase;
import com.automatas.shared.domain.port.out.DatabasePort;
import com.automatas.semantico.domain.port.out.LexicalPort;
import com.automatas.shared.domain.port.out.OllamaPort;
import com.automatas.semantico.domain.port.out.SyntacticPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orquestador principal del Mini-Compilador.
 * Coordina las fases lexica, sintactica y semantica a traves de microservicios.
 * Patron Bridge: usa las abstracciones LexicalPort y SyntacticPort.
 */
@Service
public class AnalyzeQueryService implements AnalyzeQueryUseCase {

    private final QueryNormalizer queryNormalizer;
    private final NlpSegmentService nlpSegmentService;
    private final LexicalPort lexicalPort;
    private final SyntacticPort syntacticPort;
    private final SemanticService semanticService;
    private final OllamaPort ollamaPort;
    private final DatabasePort databasePort;

    public AnalyzeQueryService(
            QueryNormalizer queryNormalizer,
            NlpSegmentService nlpSegmentService,
            LexicalPort lexicalPort,
            SyntacticPort syntacticPort,
            SemanticService semanticService,
            OllamaPort ollamaPort,
            DatabasePort databasePort
    ) {
        this.queryNormalizer = queryNormalizer;
        this.nlpSegmentService = nlpSegmentService;
        this.lexicalPort = lexicalPort;
        this.syntacticPort = syntacticPort;
        this.semanticService = semanticService;
        this.ollamaPort = ollamaPort;
        this.databasePort = databasePort;
    }

    @Override
    public AnalysisResult analyze(String query) {
        String normalizedQuery = queryNormalizer.normalize(query);
        List<PhaseError> phaseErrors = new ArrayList<>();

        // 1. Fase Lexica (via microservicio)
        LexicalResult lexicalResult = lexicalPort.analyze(normalizedQuery);
        if (!lexicalResult.errors().isEmpty()) {
            phaseErrors.add(new PhaseError("lexico", "Se detectaron tokens invalidos en la consulta"));
        }

        Map<String, Object> astFormal = null;
        AstNode astNode = null;
        List<String> semanticErrors = List.of();
        Map<String, Object> astLlm = null;
        String astLlmMessage = null;

        // 2. Fase Sintactica (via microservicio) - solo si no hay errores lexico
        if (lexicalResult.errors().isEmpty()) {
            try {
                // Convertir tokens a mapas para el envio HTTP
                List<Map<String, Object>> tokenMaps = lexicalResult.tokens().stream()
                        .map(t -> Map.<String, Object>of(
                                "type", t.type(),
                                "value", t.value(),
                                "line", t.line(),
                                "column", t.column()))
                        .toList();

                SyntacticResult syntacticResult = syntacticPort.analyze(query, tokenMaps);

                astFormal = syntacticResult.ast();
                astLlm = syntacticResult.astLlm();
                astLlmMessage = syntacticResult.llmMessage();

                // Reconstruir AST para la fase semantica
                if (astFormal != null) {
                    astNode = rebuildAstNode(astFormal);
                }

                // Errores sintacticos
                for (SyntacticError err : syntacticResult.errors()) {
                    phaseErrors.add(new PhaseError(err.phase(), err.message()));
                }

                // 3. Fase Semantica (local)
                if (astNode != null) {
                    semanticErrors = semanticService.analyze(astNode);
                }
            } catch (Exception ex) {
                phaseErrors.add(new PhaseError("sintactico", ex.getMessage()));
            }
        }

        boolean valid = phaseErrors.isEmpty()
                && semanticErrors.isEmpty()
                && astNode != null
                && lexicalResult.errors().isEmpty();

        // 4. Ejecucion en PostgreSQL
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
                astLlm,
                ollamaPort.isAvailable(),
                astLlmMessage,
                Map.of(),
                semanticErrors,
                phaseErrors,
                valid,
                executionResult
        );
    }

    /**
     * Reconstruye un AstNode desde un Map (JSON del microservicio sintactico).
     */
    @SuppressWarnings("unchecked")
    private AstNode rebuildAstNode(Map<String, Object> map) {
        String type = (String) map.get("type");
        return switch (type) {
            case "SELECT" -> {
                List<String> columns = (List<String>) map.get("columns");
                String table = (String) map.get("table");
                List<AstNode.Condition> where = parseConditions((List<Map<String, Object>>) map.get("where"));
                yield new AstNode.SelectNode(columns, table, where);
            }
            case "INSERT" -> {
                String table = (String) map.get("table");
                List<String> columns = (List<String>) map.get("columns");
                List<AstNode.Value> values = parseValues((List<Map<String, Object>>) map.get("values"));
                yield new AstNode.InsertNode(table, columns, values);
            }
            case "UPDATE" -> {
                String table = (String) map.get("table");
                List<AstNode.Assignment> assignments = parseAssignments((List<Map<String, Object>>) map.get("assignments"));
                List<AstNode.Condition> where = parseConditions((List<Map<String, Object>>) map.get("where"));
                yield new AstNode.UpdateNode(table, assignments, where);
            }
            case "DELETE" -> {
                String table = (String) map.get("table");
                List<AstNode.Condition> where = parseConditions((List<Map<String, Object>>) map.get("where"));
                yield new AstNode.DeleteNode(table, where);
            }
            default -> throw new IllegalArgumentException("Tipo de nodo no soportado: " + type);
        };
    }

    @SuppressWarnings("unchecked")
    private List<AstNode.Condition> parseConditions(List<Map<String, Object>> conditionMaps) {
        if (conditionMaps == null) return List.of();
        return conditionMaps.stream().map(m -> new AstNode.Condition(
                (String) m.get("column"),
                (String) m.get("operator"),
                parseValue((Map<String, Object>) m.get("value")),
                (String) m.get("connector")
        )).toList();
    }

    @SuppressWarnings("unchecked")
    private List<AstNode.Value> parseValues(List<Map<String, Object>> valueMaps) {
        if (valueMaps == null) return List.of();
        return valueMaps.stream().map(this::parseValue).toList();
    }

    @SuppressWarnings("unchecked")
    private AstNode.Value parseValue(Map<String, Object> map) {
        if (map == null) return null;
        return new AstNode.Value((String) map.get("kind"), map.get("value"));
    }

    @SuppressWarnings("unchecked")
    private List<AstNode.Assignment> parseAssignments(List<Map<String, Object>> assignmentMaps) {
        if (assignmentMaps == null) return List.of();
        return assignmentMaps.stream().map(m -> new AstNode.Assignment(
                (String) m.get("column"),
                parseValue((Map<String, Object>) m.get("value"))
        )).toList();
    }
}
