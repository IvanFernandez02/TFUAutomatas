package com.automatas.sqlcompiler.application.service;

import com.automatas.sqlcompiler.domain.model.LexicalError;
import com.automatas.sqlcompiler.domain.model.LexicalResult;
import com.automatas.sqlcompiler.domain.model.Token;
import com.automatas.sqlcompiler.domain.port.out.OllamaPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LexicalService {

    private static final Set<String> RESERVED_WORDS = Set.of(
            "SELECCIONAR", "DESDE", "CUANDO", "DONDE", "INSERTAR", "EN", "VALORES",
            "MODIFICAR", "ESTABLECER", "ELIMINAR", "Y", "O"
    );

    private static final List<TokenPattern> TOKEN_PATTERNS = List.of(
            new TokenPattern("SELECCIONAR", "(?i)seleccionar"),
            new TokenPattern("DESDE", "(?i)desde"),
            new TokenPattern("CUANDO", "(?i)cuando|donde"),
            new TokenPattern("INSERTAR", "(?i)insertar"),
            new TokenPattern("EN", "(?i)en"),
            new TokenPattern("VALORES", "(?i)valores"),
            new TokenPattern("MODIFICAR", "(?i)modificar"),
            new TokenPattern("ESTABLECER", "(?i)establecer"),
            new TokenPattern("ELIMINAR", "(?i)eliminar"),
            new TokenPattern("Y", "(?i)y"),
            new TokenPattern("O", "(?i)o"),
            new TokenPattern("MAYORIGUAL", "[>]="),
            new TokenPattern("MENORIGUAL", "[<]="),
            new TokenPattern("DISTINTO", "[<][>]"),
            new TokenPattern("IGUAL", "="),
            new TokenPattern("MAYOR", "[>]"),
            new TokenPattern("MENOR", "[<]"),
            new TokenPattern("ASTERISCO", "\\*"),
            new TokenPattern("CADENA", "'[^']*'"),
            new TokenPattern("NUMERO", "\\d+(?:\\.\\d+)?"),
            new TokenPattern("IDENTIFICADOR", "[A-Za-z_][A-Za-z0-9_]*"),
            new TokenPattern("COMA", ","),
            new TokenPattern("LPAREN", "\\("),
            new TokenPattern("RPAREN", "\\)"),
            new TokenPattern("SPACE", "\\s+"),
            new TokenPattern("UNKNOWN", "[^\\s]+")
    );

    private final OllamaPort ollamaPort;
    private final ObjectMapper objectMapper;

    public LexicalService(OllamaPort ollamaPort, ObjectMapper objectMapper) {
        this.ollamaPort = ollamaPort;
        this.objectMapper = objectMapper;
    }

    public LexicalResult analyze(String source) {
        Pattern pattern = buildPattern();
        List<Token> tokens = new ArrayList<>();
        List<LexicalError> errors = new ArrayList<>();

        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            String tokenType = resolveTokenType(matcher);
            String tokenValue = matcher.group(tokenType);

            if ("SPACE".equals(tokenType)) {
                continue;
            }

            String normalizedValue = normalizeValue(tokenType, tokenValue);
            tokens.add(new Token(tokenType, normalizedValue, 1, matcher.start(tokenType)));

            if ("UNKNOWN".equals(tokenType)) {
                errors.add(new LexicalError(tokenValue, matcher.start(tokenType)));
            }
        }

        Map<String, Object> explanation = Map.of();
        if (!errors.isEmpty()) {
            explanation = explainErrors(source, errors);
        }

        return new LexicalResult(tokens, errors, explanation);
    }

    private String resolveTokenType(Matcher matcher) {
        for (TokenPattern tokenPattern : TOKEN_PATTERNS) {
            if (matcher.group(tokenPattern.name) != null) {
                return tokenPattern.name;
            }
        }
        return "UNKNOWN";
    }

    private Map<String, Object> explainErrors(String source, List<LexicalError> errors) {
        if (!ollamaPort.isAvailable()) {
            return fallbackExplanation(errors, "Ollama no disponible para explicar errores léxicos.");
        }

        try {
            String prompt = buildPrompt(source, errors);
            String response = ollamaPort.generate(prompt);
            return objectMapper.readValue(response, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return fallbackExplanation(errors, "La respuesta del modelo no fue un JSON válido.");
        }
    }

    private Map<String, Object> fallbackExplanation(List<LexicalError> errors, String cause) {
        List<Map<String, String>> items = new ArrayList<>();
        for (LexicalError error : errors) {
            items.add(Map.of(
                    "token", error.token(),
                    "cause", cause,
                    "suggestion", "Elimine el token inválido."
            ));
        }
        return Map.of("errors", items);
    }

    private String buildPrompt(String source, List<LexicalError> errors) throws Exception {
        List<Map<String, Object>> errorPayload = errors.stream()
                .map(error -> Map.<String, Object>of("token", error.token(), "position", error.position()))
                .toList();

        return """
                Eres un experto en compiladores y análisis léxico SQL en español.

                Palabras reservadas:
                %s

                Tokens válidos del lenguaje:
                - SELECCIONAR, DESDE, CUANDO, DONDE, INSERTAR, EN, VALORES, MODIFICAR, ESTABLECER, ELIMINAR, Y, O
                - IDENTIFICADOR: nombres de tablas o columnas
                - NUMERO: enteros o decimales
                - CADENA: texto entre comillas simples
                - Operadores: =, >, <, >=, <=, <>
                - Separadores: , ( )

                Consulta SQL:
                %s

                Errores detectados:
                %s

                Reglas para las sugerencias:
                1. Utiliza únicamente palabras reservadas y tokens válidos definidos anteriormente.
                2. No inventes nuevas palabras reservadas.
                3. Si el token es similar a una palabra reservada SQL en español, sugiere la más cercana.
                4. Si contiene caracteres no permitidos, sugiere eliminarlos o reemplazarlos por un token válido.
                5. Si no existe una corrección razonable, utiliza: "Elimine el token inválido."

                Responde ÚNICAMENTE con JSON válido en este formato:
                {"errors":[{"token":"...","cause":"...","suggestion":"..."}]}
                """.formatted(
                objectMapper.writeValueAsString(RESERVED_WORDS.stream().sorted().toList()),
                source,
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorPayload)
        );
    }

    private Pattern buildPattern() {
        String combined = TOKEN_PATTERNS.stream()
                .map(tp -> "(?<" + tp.name + ">" + tp.regex + ")")
                .reduce((left, right) -> left + "|" + right)
                .orElseThrow();
        return Pattern.compile(combined);
    }

    private String normalizeValue(String tokenType, String tokenValue) {
        if ("CADENA".equals(tokenType)) {
            return tokenValue.substring(1, tokenValue.length() - 1);
        }
        if ("IDENTIFICADOR".equals(tokenType)) {
            return tokenValue.toUpperCase();
        }
        if ("CUANDO".equals(tokenType)) {
            return tokenValue.toUpperCase();
        }
        return tokenValue;
    }

    private record TokenPattern(String name, String regex) {
    }
}
