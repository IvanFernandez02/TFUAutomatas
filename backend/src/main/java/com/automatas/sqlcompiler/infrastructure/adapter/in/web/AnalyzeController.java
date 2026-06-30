package com.automatas.sqlcompiler.infrastructure.adapter.in.web;

import com.automatas.sqlcompiler.domain.port.in.AnalyzeQueryUseCase;
import com.automatas.sqlcompiler.infrastructure.adapter.in.web.dto.AnalyzeRequest;
import com.automatas.sqlcompiler.infrastructure.adapter.in.web.dto.AnalyzeResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final AnalyzeQueryUseCase analyzeQueryUseCase;

    public AnalyzeController(AnalyzeQueryUseCase analyzeQueryUseCase) {
        this.analyzeQueryUseCase = analyzeQueryUseCase;
    }

    @PostMapping("/analyze")
    public AnalyzeResponseDto analyze(@RequestBody AnalyzeRequest request) {
        if (request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("La consulta no puede estar vacía");
        }
        return AnalyzeResponseDto.from(analyzeQueryUseCase.analyze(request.query()));
    }
}
