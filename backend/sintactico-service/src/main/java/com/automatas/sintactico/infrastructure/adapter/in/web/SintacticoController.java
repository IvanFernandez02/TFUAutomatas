package com.automatas.sintactico.infrastructure.adapter.in.web;

import com.automatas.shared.domain.model.SyntacticResult;
import com.automatas.sintactico.domain.port.in.SyntacticAnalyzer;
import com.automatas.sintactico.infrastructure.adapter.in.web.dto.SyntacticRequest;
import com.automatas.sintactico.infrastructure.adapter.in.web.dto.SyntacticResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SintacticoController {

    private final SyntacticAnalyzer syntacticAnalyzer;

    public SintacticoController(SyntacticAnalyzer syntacticAnalyzer) {
        this.syntacticAnalyzer = syntacticAnalyzer;
    }

    @PostMapping("/syntactic")
    public SyntacticResponseDto analyze(@RequestBody SyntacticRequest request) {
        if (request.tokens() == null || request.tokens().isEmpty()) {
            throw new IllegalArgumentException("La lista de tokens no puede estar vacia");
        }
        SyntacticResult result = syntacticAnalyzer.analyze(
                request.query() != null ? request.query() : "",
                request.tokens()
        );
        return SyntacticResponseDto.from(result);
    }
}
