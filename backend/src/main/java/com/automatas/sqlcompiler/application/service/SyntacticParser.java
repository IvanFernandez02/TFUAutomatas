package com.automatas.sqlcompiler.application.service;

import com.automatas.sqlcompiler.domain.model.Token;
import com.automatas.sqlcompiler.domain.model.ast.AstNode;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.Assignment;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.Condition;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.DeleteNode;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.InsertNode;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.SelectNode;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.UpdateNode;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SyntacticParser {

    private List<Token> tokens;
    private int index;

    public AstNode parse(List<Token> tokens) {
        this.tokens = tokens;
        this.index = 0;

        if (tokens.isEmpty()) {
            throw new SyntaxException("Error sintáctico: consulta vacía");
        }

        return switch (peek().type()) {
            case "SELECCIONAR" -> parseSelect();
            case "INSERTAR" -> parseInsert();
            case "MODIFICAR" -> parseUpdate();
            case "ELIMINAR" -> parseDelete();
            default -> throw new SyntaxException("Error sintáctico: sentencia no reconocida near '" + peek().value() + "'");
        };
    }

    private SelectNode parseSelect() {
        expect("SELECCIONAR");
        List<String> columns = parseColumnList();
        expect("DESDE");
        String table = expectIdentifier();
        List<Condition> where = List.of();
        if (match("CUANDO")) {
            where = parseConditions();
        }
        return new SelectNode(columns, table, where);
    }

    private InsertNode parseInsert() {
        expect("INSERTAR");
        expect("EN");
        String table = expectIdentifier();
        expect("LPAREN");
        List<String> columns = parseIdentifierList();
        expect("RPAREN");
        expect("VALORES");
        expect("LPAREN");
        List<Value> values = parseValueList();
        expect("RPAREN");
        return new InsertNode(table, columns, values);
    }

    private UpdateNode parseUpdate() {
        expect("MODIFICAR");
        String table = expectIdentifier();
        expect("ESTABLECER");
        List<Assignment> assignments = parseAssignments();
        List<Condition> where = List.of();
        if (match("CUANDO")) {
            where = parseConditions();
        }
        return new UpdateNode(table, assignments, where);
    }

    private DeleteNode parseDelete() {
        expect("ELIMINAR");
        expect("DESDE");
        String table = expectIdentifier();
        List<Condition> where = List.of();
        if (match("CUANDO")) {
            where = parseConditions();
        }
        return new DeleteNode(table, where);
    }

    private List<String> parseColumnList() {
        if (match("ASTERISCO")) {
            return List.of("*");
        }
        return parseIdentifierList();
    }

    private List<String> parseIdentifierList() {
        List<String> items = new ArrayList<>();
        items.add(expectIdentifier());
        while (match("COMA")) {
            items.add(expectIdentifier());
        }
        return items;
    }

    private List<Value> parseValueList() {
        List<Value> items = new ArrayList<>();
        items.add(parseValue());
        while (match("COMA")) {
            items.add(parseValue());
        }
        return items;
    }

    private List<Assignment> parseAssignments() {
        List<Assignment> assignments = new ArrayList<>();
        String column = expectIdentifier();
        expect("IGUAL");
        assignments.add(new Assignment(column, parseValue()));
        while (match("COMA")) {
            column = expectIdentifier();
            expect("IGUAL");
            assignments.add(new Assignment(column, parseValue()));
        }
        return assignments;
    }

    private List<Condition> parseConditions() {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(parseCondition(null));
        while (true) {
            if (match("Y")) {
                attachConnector(conditions, "Y");
                conditions.add(parseCondition(null));
            } else if (match("O")) {
                attachConnector(conditions, "O");
                conditions.add(parseCondition(null));
            } else {
                break;
            }
        }
        return conditions;
    }

    private void attachConnector(List<Condition> conditions, String connector) {
        Condition last = conditions.remove(conditions.size() - 1);
        conditions.add(new Condition(last.column(), last.operator(), last.value(), connector));
    }

    private Condition parseCondition(String connector) {
        String column = expectIdentifier();
        String operator = parseOperator();
        Value value = parseValue();
        return new Condition(column, operator, value, connector);
    }

    private String parseOperator() {
        Token token = peek();
        return switch (token.type()) {
            case "IGUAL" -> {
                advance();
                yield "=";
            }
            case "MAYOR" -> {
                advance();
                yield ">";
            }
            case "MENOR" -> {
                advance();
                yield "<";
            }
            case "MAYORIGUAL" -> {
                advance();
                yield ">=";
            }
            case "MENORIGUAL" -> {
                advance();
                yield "<=";
            }
            case "DISTINTO" -> {
                advance();
                yield "<>";
            }
            default -> throw new SyntaxException("Error sintáctico: operador inválido near '" + token.value() + "'");
        };
    }

    private Value parseValue() {
        Token token = peek();
        return switch (token.type()) {
            case "NUMERO" -> {
                advance();
                yield new Value("number", parseNumber(token.value()));
            }
            case "CADENA" -> {
                advance();
                yield new Value("string", token.value());
            }
            case "IDENTIFICADOR" -> {
                advance();
                yield new Value("identifier", token.value());
            }
            default -> throw new SyntaxException("Error sintáctico: valor inválido near '" + token.value() + "'");
        };
    }

    private Object parseNumber(String raw) {
        if (raw.contains(".")) {
            return Double.parseDouble(raw);
        }
        return Integer.parseInt(raw);
    }

    private String expectIdentifier() {
        Token token = peek();
        if (!"IDENTIFICADOR".equals(token.type())) {
            throw new SyntaxException("Error sintáctico: se esperaba identificador near '" + token.value() + "'");
        }
        advance();
        return token.value();
    }

    private void expect(String type) {
        Token token = peek();
        if (!type.equals(token.type())) {
            throw new SyntaxException("Error sintáctico: se esperaba " + type + " near '" + token.value() + "'");
        }
        advance();
    }

    private boolean match(String type) {
        if (index < tokens.size() && type.equals(tokens.get(index).type())) {
            advance();
            return true;
        }
        return false;
    }

    private Token peek() {
        if (index >= tokens.size()) {
            throw new SyntaxException("Error sintáctico: entrada incompleta o inválida");
        }
        return tokens.get(index);
    }

    private void advance() {
        index++;
    }

    public static class SyntaxException extends RuntimeException {
        public SyntaxException(String message) {
            super(message);
        }
    }
}
