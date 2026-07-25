package com.automatas.semantico.application;

import com.automatas.shared.domain.model.NlpSegment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NlpSegmentService {

    private static final Set<String> RESERVED = Set.of(
            "SELECCIONAR", "DESDE", "CUANDO", "DONDE", "INSERTAR", "EN", "VALORES",
            "MODIFICAR", "ESTABLECER", "ELIMINAR", "Y", "O"
    );

    private static final Pattern SEGMENT_PATTERN = Pattern.compile(
            "SELECCIONAR|DESDE|CUANDO|DONDE|INSERTAR|EN|VALORES|MODIFICAR|ESTABLECER|ELIMINAR|"
                    + "Y|O|>=|<=|<>|=|>|<|\\*|\\(|\\)|,|'[^']*'|\\d+(?:\\.\\d+)?|[A-Za-z_][A-Za-z0-9_]*",
            Pattern.CASE_INSENSITIVE
    );

    public List<NlpSegment> segment(String normalizedQuery) {
        List<NlpSegment> segments = new ArrayList<>();
        Matcher matcher = SEGMENT_PATTERN.matcher(normalizedQuery);
        while (matcher.find()) {
            String text = matcher.group().toUpperCase();
            segments.add(new NlpSegment(text, classify(text)));
        }
        return segments;
    }

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
        return "IDENTIFICADOR";
    }
}
