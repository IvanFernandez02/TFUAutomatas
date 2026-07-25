package com.automatas.sintactico.application;

import com.automatas.shared.domain.model.SyntacticError;
import com.automatas.shared.domain.model.SyntacticResult;
import com.automatas.shared.domain.model.Token;
import com.automatas.shared.domain.model.ast.AstNode;
import com.automatas.sintactico.domain.port.in.SyntacticAnalyzer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementacion combinada del analizador sintactico (Patron Bridge).
 * Coordina el parser formal y el LLM para producir ambos ASTs.
 */
@Component
public class CombinedSyntacticAnalyzer implements SyntacticAnalyzer {

    private final FormalSyntacticParser formalParser;
    private final LlmSyntacticAnalyzer llmAnalyzer;

    public CombinedSyntacticAnalyzer(FormalSyntacticParser formalParser,
                                      LlmSyntacticAnalyzer llmAnalyzer) {
        this.formalParser = formalParser;
        this.llmAnalyzer = llmAnalyzer;
    }

    @Override
    public SyntacticResult analyze(String originalQuery, List<Map<String, Object>> tokenMaps) {
        List<Token> tokens = tokenMaps.stream()
                .map(m -> new Token(
                        (String) m.get("type"),
                        (String) m.get("value"),
                        m.get("line") != null ? ((Number) m.get("line")).intValue() : 0,
                        m.get("column") != null ? ((Number) m.get("column")).intValue() : 0
                ))
                .toList();

        Map<String, Object> astFormal = null;
        List<SyntacticError> errors = new ArrayList<>();

        // Parser formal
        try {
            AstNode astNode = formalParser.parse(tokens);
            astFormal = astNode.toMap();
        } catch (FormalSyntacticParser.SyntaxException ex) {
            errors.add(new SyntacticError("sintactico", ex.getMessage()));
        }

        // LLM (ejecuta siempre, es informativo)
        LlmSyntacticAnalyzer.LlmAstResult llmResult =
                llmAnalyzer.generateAst(originalQuery, tokens);

        return new SyntacticResult(
                astFormal,
                llmResult.ast(),
                llmResult.message(),
                errors
        );
    }
}
