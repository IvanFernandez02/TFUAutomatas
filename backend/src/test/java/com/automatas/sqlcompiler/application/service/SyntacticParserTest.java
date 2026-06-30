package com.automatas.sqlcompiler.application.service;

import com.automatas.sqlcompiler.domain.model.Token;
import com.automatas.sqlcompiler.domain.model.ast.AstNode.SelectNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SyntacticParserTest {

    private final SyntacticParser parser = new SyntacticParser();

    @Test
    void parsesSelectStatement() {
        List<Token> tokens = List.of(
                new Token("SELECCIONAR", "SELECCIONAR", 1, 0),
                new Token("IDENTIFICADOR", "NOMBRE", 1, 12),
                new Token("COMA", ",", 1, 18),
                new Token("IDENTIFICADOR", "EDAD", 1, 20),
                new Token("DESDE", "DESDE", 1, 25),
                new Token("IDENTIFICADOR", "USUARIOS", 1, 31),
                new Token("CUANDO", "CUANDO", 1, 40),
                new Token("IDENTIFICADOR", "EDAD", 1, 46),
                new Token("MAYOR", ">", 1, 51),
                new Token("NUMERO", "18", 1, 53)
        );

        SelectNode node = (SelectNode) parser.parse(tokens);
        assertEquals("USUARIOS", node.table());
        assertEquals(List.of("NOMBRE", "EDAD"), node.columns());
        assertEquals(1, node.where().size());
    }

    @Test
    void rejectsIncompleteSelect() {
        List<Token> tokens = List.of(
                new Token("SELECCIONAR", "SELECCIONAR", 1, 0),
                new Token("IDENTIFICADOR", "NOMBRE", 1, 12),
                new Token("DESDE", "DESDE", 1, 18)
        );

        assertThrows(SyntacticParser.SyntaxException.class, () -> parser.parse(tokens));
    }
}
