package com.automatas.shared.domain.port.out;

import com.automatas.shared.domain.model.ExecutionResult;
import com.automatas.shared.domain.model.ast.AstNode;

import java.util.List;
import java.util.Map;

public interface DatabasePort {

    ExecutionResult execute(AstNode node);

    List<Map<String, Object>> queryTable(String tableName);
}
