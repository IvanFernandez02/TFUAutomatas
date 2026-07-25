package com.automatas.sintactico.infrastructure.adapter.in.web.dto;

import java.util.List;
import java.util.Map;

public record SyntacticRequest(
        String query,
        List<Map<String, Object>> tokens
) {
}
