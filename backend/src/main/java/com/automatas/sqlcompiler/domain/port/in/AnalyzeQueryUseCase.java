package com.automatas.sqlcompiler.domain.port.in;

import com.automatas.sqlcompiler.domain.model.AnalysisResult;

public interface AnalyzeQueryUseCase {

    AnalysisResult analyze(String query);
}
