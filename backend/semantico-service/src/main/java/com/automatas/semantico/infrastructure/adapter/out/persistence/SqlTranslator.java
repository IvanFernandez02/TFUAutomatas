package com.automatas.semantico.infrastructure.adapter.out.persistence;

import com.automatas.shared.domain.model.ast.AstNode;
import com.automatas.shared.domain.model.ast.AstNode.Assignment;
import com.automatas.shared.domain.model.ast.AstNode.Condition;
import com.automatas.shared.domain.model.ast.AstNode.DeleteNode;
import com.automatas.shared.domain.model.ast.AstNode.InsertNode;
import com.automatas.shared.domain.model.ast.AstNode.SelectNode;
import com.automatas.shared.domain.model.ast.AstNode.UpdateNode;
import com.automatas.shared.domain.model.ast.AstNode.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SqlTranslator {

    public record TranslatedSql(String sql, List<Object> params) {}

    public TranslatedSql translate(AstNode node) {
        return switch (node) {
            case SelectNode select -> translateSelect(select);
            case InsertNode insert -> translateInsert(insert);
            case UpdateNode update -> translateUpdate(update);
            case DeleteNode delete -> translateDelete(delete);
            default -> throw new IllegalArgumentException("Tipo de nodo no soportado: " + node.type());
        };
    }

    private TranslatedSql translateSelect(SelectNode node) {
        List<Object> params = new ArrayList<>();
        String columns = node.columns().equals(List.of("*"))
                ? "*"
                : String.join(", ", node.columns());

        StringBuilder sql = new StringBuilder("SELECT ")
                .append(columns)
                .append(" FROM ")
                .append(node.table());

        appendWhere(sql, node.where(), params);
        return new TranslatedSql(sql.toString(), params);
    }

    private TranslatedSql translateInsert(InsertNode node) {
        List<Object> params = new ArrayList<>();
        String columns = String.join(", ", node.columns());
        String placeholders = node.values().stream()
                .map(v -> "?")
                .collect(Collectors.joining(", "));

        for (Value value : node.values()) {
            params.add(extractValue(value));
        }

        String sql = "INSERT INTO " + node.table()
                + " (" + columns + ") VALUES (" + placeholders + ")";
        return new TranslatedSql(sql, params);
    }

    private TranslatedSql translateUpdate(UpdateNode node) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("UPDATE ")
                .append(node.table())
                .append(" SET ");

        List<String> setParts = new ArrayList<>();
        for (Assignment assignment : node.assignments()) {
            setParts.add(assignment.column() + " = ?");
            params.add(extractValue(assignment.value()));
        }
        sql.append(String.join(", ", setParts));

        appendWhere(sql, node.where(), params);
        return new TranslatedSql(sql.toString(), params);
    }

    private TranslatedSql translateDelete(DeleteNode node) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("DELETE FROM ")
                .append(node.table());

        appendWhere(sql, node.where(), params);
        return new TranslatedSql(sql.toString(), params);
    }

    private void appendWhere(StringBuilder sql, List<Condition> conditions, List<Object> params) {
        if (conditions == null || conditions.isEmpty()) {
            return;
        }

        sql.append(" WHERE ");
        for (int i = 0; i < conditions.size(); i++) {
            Condition condition = conditions.get(i);
            if (i > 0) {
                Condition prev = conditions.get(i - 1);
                if ("Y".equals(prev.connector())) {
                    sql.append(" AND ");
                } else if ("O".equals(prev.connector())) {
                    sql.append(" OR ");
                } else {
                    sql.append(" AND ");
                }
            }
            sql.append(condition.column())
               .append(" ")
               .append(condition.operator())
               .append(" ?");
            params.add(extractValue(condition.value()));
        }
    }

    private Object extractValue(Value value) {
        return value.value();
    }
}
