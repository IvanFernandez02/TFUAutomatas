package com.automatas.lexico.infrastructure.adapter.in.web;

import com.automatas.shared.domain.model.LexicalResult;
import com.automatas.lexico.domain.port.in.LexicalAnalyzer;
import com.automatas.lexico.infrastructure.adapter.in.web.dto.LexicalRequest;
import com.automatas.lexico.infrastructure.adapter.in.web.dto.LexicalResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LexicoController {

    private final LexicalAnalyzer lexicalAnalyzer;

    public LexicoController(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    @PostMapping("/lexical")
    public LexicalResponseDto analyze(@RequestBody LexicalRequest request) {
        if (request.source() == null || request.source().isBlank()) {
            throw new IllegalArgumentException("El texto fuente no puede estar vacio");
        }
        LexicalResult result = lexicalAnalyzer.analyze(request.source());
        return LexicalResponseDto.from(result);
    }
}
