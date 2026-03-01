import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import compiler.Lexer.Symbol;
import compiler.Lexer.TokenType;
import org.junit.Test;

import java.io.StringReader;
import compiler.Lexer.Lexer;

public class TestLexer {
    /** check if the lexer ignore comment */
    @Test
    public void testComment() {
        String input = "INT # commentaire \n x";
        Lexer lexer = new Lexer(new StringReader(input));

        Symbol symbolInt = lexer.getNextSymbol();
        assertEquals(TokenType.INT, symbolInt.getType());

        Symbol identifierX = lexer.getNextSymbol();
        assertEquals(TokenType.IDENTIFIER, identifierX.getType());
        assertEquals("x", identifierX.getValue());
    }

    /** check if the lexer detects correctly the syntaxe for identifiers and collections
     * identifier must start with a non-capital letter or underscore
     * otherwise it's a collection
     */
    @Test
    public void testIdentifierAndCollection() {
        String identifier = "aBc_123";
        String collection = "BSAA";
        Lexer identifierLexer = new Lexer(new StringReader(identifier));
        Lexer collectionLexer = new Lexer(new StringReader(collection));

        Symbol identifierSymbol = identifierLexer.getNextSymbol();
        assertEquals(TokenType.IDENTIFIER, identifierSymbol.getType());
        assertEquals("aBc_123", identifierSymbol.getValue());

        Symbol collectionSymbol = collectionLexer.getNextSymbol();
        assertEquals(TokenType.COLLECTION, collectionSymbol.getType());
        assertEquals("BSAA", collectionSymbol.getValue());
    }

    /** check if the lexer recognize 00324 as an integer 324 */
    @Test
    public void testInteger() {
        String input = "00324";
        Lexer lexer = new Lexer(new StringReader(input));

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenType.INT_VALUE, symbol.getType());
        assertEquals(324, symbol.getValue());
    }

    /** check if the lexer recognize .234 as a float 0.234 */
    @Test
    public void testFloat() {
        String input = ".234";
        Lexer lexer = new Lexer(new StringReader(input));

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenType.FLOAT_VALUE, symbol.getType());
        assertEquals(0.234f, symbol.getValue());
    }

    /** handle unrecognized token like @ */
    @Test
    public void testUnrecognizedToken() {
        String input = "@";
        Lexer lexer = new Lexer(new StringReader(input));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            lexer.getNextSymbol();
        });

        assertTrue(exception.getMessage().contains("special symbol : unknown caracter @"));
    }

}
