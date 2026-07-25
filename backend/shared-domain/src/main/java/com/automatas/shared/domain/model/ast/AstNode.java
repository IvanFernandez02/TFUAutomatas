package com.automatas.shared.domain.model.ast;

import java.util.List;
import java.util.Map;

public interface AstNode {

    String type();

    Map<String, Object> toMap();

    record Value(String kind, Object value) {
        public Map<String, Object> toMap() {
            return Map.of("kind", kind, "value", value);
        }
    }

    record Condition(String column, String operator, Value value, String connector) {
        public Map<String, Object> toMap() {
            if (connector == null) {
                return Map.of("column", column, "operator", operator, "value", value.toMap());
            }
            return Map.of(
                    "column", column,
                    "operator", operator,
                    "value", value.toMap(),
                    "connector", connector
            );
        }
    }

    record Assignment(String column, Value value) {
        public Map<String, Object> toMap() {
            return Map.of("column", column, "value", value.toMap());
        }
    }

    record SelectNode(List<String> columns, String table, List<Condition> where) implements AstNode {
        @Override
        public String type() {
            return "SELECT";
        }

        @Override
        public Map<String, Object> toMap() {
            return Map.of(
                    "type", type(),
                    "columns", columns,
                    "table", table,
                    "where", where.stream().map(Condition::toMap).toList()
            );
        }
    }

    record InsertNode(String table, List<String> columns, List<Value> values) implements AstNode {
        @Override
        public String type() {
            return "INSERT";
        }

        @Override
        public Map<String, Object> toMap() {
            return Map.of(
                    "type", type(),
                    "table", table,
                    "columns", columns,
                    "values", values.stream().map(Value::toMap).toList()
            );
        }
    }

    record UpdateNode(String table, List<Assignment> assignments, List<Condition> where) implements AstNode {
        @Override
        public String type() {
            return "UPDATE";
        }

        @Override
        public Map<String, Object> toMap() {
            return Map.of(
                    "type", type(),
                    "table", table,
                    "assignments", assignments.stream().map(Assignment::toMap).toList(),
                    "where", where.stream().map(Condition::toMap).toList()
            );
        }
    }

    record DeleteNode(String table, List<Condition> where) implements AstNode {
        @Override
        public String type() {
            return "DELETE";
        }

        @Override
        public Map<String, Object> toMap() {
            return Map.of(
                    "type", type(),
                    "table", table,
                    "where", where.stream().map(Condition::toMap).toList()
            );
        }
    }
}
