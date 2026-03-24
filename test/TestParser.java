import static org.junit.Assert.assertTrue;
import compiler.Lexer.Lexer;
import compiler.Parser.Parser;
import compiler.AST.basic.ProgramNode;
import org.junit.Test;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestParser {

    // for getting the AST output
    private String getASToutput(String input) {
        Lexer lexer = new Lexer(new StringReader(input));
        Parser parser = new Parser(lexer);
        ProgramNode root = parser.getAST();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        root.print(0);
        String output = outContent.toString();
        System.setOut(originalOut);
        return output;
    }

    /** test the tree structure of variable declaration nodes and print output */
    @Test
    public void testVariableASTparse() {
        String input = "final FLOAT pi = 3.14;";
        String output = getASToutput(input);

        assertTrue("output contain VariableDeclaration",output.contains("VariableDeclaration"));
        assertTrue("output contain float",output.contains("float"));
        assertTrue("output contain pi",output.contains("pi"));
        assertTrue("output contain final",output.contains("final"));
        assertTrue("output contain Float_LITERAL",output.contains("Float_LITERAL, 3.14"));
    }

    /** test the precedence structure of binary operations */
    @Test
    public void testBinaryOpASTparse() {
        String input = "INT res = 1 + 2 * 3;";
        String output = getASToutput(input);

        assertTrue("output contain res", output.contains("VariableDeclaration, int res"));
        assertTrue("output contain binary operation", output.contains("binary operation: null"));
        assertTrue("output contain operand 1", output.contains("Integer_LITERAL, 1"));
        assertTrue("output contain operand 2", output.contains("Integer_LITERAL, 2"));
        assertTrue("output contain operand 3", output.contains("Integer_LITERAL, 3"));

    }

    /** test the function calls and dot operators node*/
    @Test
    public void testFunctionAndDotASTparse() {
        String input = "p.x = getVal(10);";
        String output = getASToutput(input);

        assertTrue("output contain assignment node", output.contains("assignment"));
        assertTrue("output contain field node", output.contains("FieldAccess_LITERAL"));
        assertTrue("output contain the property", output.contains(".x"));
        assertTrue("output contain function call expression", output.contains("function call expression"));
        assertTrue("output contain function parameters", output.contains("Integer_LITERAL, 10"));
    }

    /** test the if-else statements node*/
    @Test
    public void testIfElseASTparse() {
        String input = "if (x > 0) { return 1; } else { return 0; }";
        String output = getASToutput(input);

        assertTrue("output contain if-else statement",output.contains("if-else statement"));
        assertTrue("output contain binary operation",output.contains("binary operation"));
        assertTrue("output contain return statement", output.contains("return statement"));
        assertTrue("output contain else", output.contains("else"));
    }

    /** test the  while statements node*/
    @Test
    public void testWhileLoopASTparse() {
        String input = "while (i < 10) { i = i + 1; }";
        String output = getASToutput(input);

        assertTrue("output contain while statement",output.contains("while statement"));
        assertTrue("output contain binary operation",output.contains("binary operation"));
        assertTrue("output contain assignment node",output.contains("assignment"));
    }

    /** test the array access node */
    @Test
    public void testArrayASTparse() {
        String input = "INT[] arr = INT ARRAY[5]; INT x = arr[0];";
        String output = getASToutput(input);

        assertTrue("output contain VariableDeclaration",output.contains("VariableDeclaration"));
        assertTrue("output contain Array Access",output.contains("ArrayAccess"));
        assertTrue("output contain the length of array",output.contains("Integer_LITERAL"));
    }

    /** test the function declaration node */
    @Test
    public void testFunctionDeclarationASTparse() {
        String input = "def INT add(INT a, INT b) { return a + b; }";
        String output = getASToutput(input);

        assertTrue("output contain FunctionDeclaration",output.contains("FunctionDefinition"));
        assertTrue("output contain add",output.contains("add(a, b)"));
        assertTrue("output contain binary operation",output.contains("binary operation"));
        assertTrue("output contain return statement",output.contains("return statement"));
        assertTrue("output contain Identifier_LITERAL", output.contains("Identifier_LITERAL"));
    }
}