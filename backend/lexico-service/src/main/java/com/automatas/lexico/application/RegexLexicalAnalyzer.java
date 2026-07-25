package com.automatas.lexico.application;

import com.automatas.shared.domain.model.LexicalError;
import com.automatas.shared.domain.model.LexicalResult;
import com.automatas.shared.domain.model.Token;
import com.automatas.lexico.domain.port.in.LexicalAnalyzer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementacion concreta del analizador lexico usando expresiones regulares.
 * Patron Bridge: esta es la implementacion concreta de la abstraccion LexicalAnalyzer.
 */
@Component
public class RegexLexicalAnalyzer implements LexicalAnalyzer {

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

    private final Pattern combinedPattern;

    public RegexLexicalAnalyzer() {
        this.combinedPattern = buildPattern();
    }

    @Override
    public LexicalResult analyze(String source) {
        List<Token> tokens = new ArrayList<>();
        List<LexicalError> errors = new ArrayList<>();

        Matcher matcher = combinedPattern.matcher(source);
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

        return new LexicalResult(tokens, errors);
    }

    private String resolveTokenType(Matcher matcher) {
        for (TokenPattern tokenPattern : TOKEN_PATTERNS) {
            if (matcher.group(tokenPattern.name) != null) {
                return tokenPattern.name;
            }
        }
        return "UNKNOWN";
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
