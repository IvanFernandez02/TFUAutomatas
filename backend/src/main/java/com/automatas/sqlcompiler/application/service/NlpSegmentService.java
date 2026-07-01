package com.automatas.sqlcompiler.application.service;

import com.automatas.sqlcompiler.domain.model.NlpSegment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de Procesamiento de Lenguaje Natural (PLN) para la segmentación y clasificación de tokens.
 * Identifica la categoría morfológica básica de cada elemento léxico en la consulta SQL.
 */
@Component
public class NlpSegmentService {

    // Palabras clave reservadas por el compilador
    private static final Set<String> RESERVED = Set.of(
            "SELECCIONAR", "DESDE", "CUANDO", "DONDE", "INSERTAR", "EN", "VALORES",
            "MODIFICAR", "ESTABLECER", "ELIMINAR", "Y", "O"
    );

    // Patrón regex que coincide con los diferentes elementos del lenguaje SQL
    private static final Pattern SEGMENT_PATTERN = Pattern.compile(
            "SELECCIONAR|DESDE|CUANDO|DONDE|INSERTAR|EN|VALORES|MODIFICAR|ESTABLECER|ELIMINAR|"
                    + "Y|O|>=|<=|<>|=|>|<|\\*|\\(|\\)|,|'[^']*'|\\d+(?:\\.\\d+)?|[A-Za-z_][A-Za-z0-9_]*",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Segmenta la consulta normalizada en unidades (tokens) clasificados con PLN.
     * 
     * @param normalizedQuery Consulta SQL previamente normalizada.
     * @return Lista de segmentos NLP conteniendo el texto y su clasificación.
     */
    public List<NlpSegment> segment(String normalizedQuery) {
        List<NlpSegment> segments = new ArrayList<>();
        Matcher matcher = SEGMENT_PATTERN.matcher(normalizedQuery);
        while (matcher.find()) {
            String text = matcher.group().toUpperCase();
            segments.add(new NlpSegment(text, classify(text)));
        }
        return segments;
    }

    /**
     * Clasifica un fragmento de texto en su categoría sintáctica/léxica correspondiente.
     */
    private String classify(String text) {
        if (RESERVED.contains(text)) {
            return "RESERVADA";
        }
        if (Set.of("=", ">", "<", ">=", "<=", "<>").contains(text)) {
            return "OPERADOR";
        }
        if (Set.of(",", "(", ")").contains(text)) {
            return "SEPARADOR";
        }
        if ("*".equals(text)) {
            return "ASTERISCO";
        }
        if (text.matches("\\d+(\\.\\d+)?")) {
            return "NUMERO";
        }
        if (text.startsWith("'")) {
            return "CADENA";
        }
        return "IDENTIFICADOR"; // Por defecto, es un nombre de tabla o columna
    }
}

