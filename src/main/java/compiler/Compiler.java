package compiler;

import compiler.Codegenerator.Codegenerator;
import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Lexer.TokenType;
import compiler.Parser.Parser;
import compiler.AST.basic.ProgramNode;
import compiler.Semantic.SymbolTable;
import java.io.FileReader;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        // INGinious
        if (args.length == 1 && !args[0].startsWith("-")) {
            runCodeGen(args[0], "test.class");
            return;
        }

        if (args.length == 3 && args[1].equals("-o")) {
            runCodeGen(args[0], args[2]);
            return;
        }

        // Local
        if (args.length == 2) {
            String mode = args[0];
            String filePath = args[1];

            if (!mode.equals("-lexer") && !mode.equals("-parser")) {
                System.err.println("Unknown mode: " + mode);
                System.err.println("Usage: -lexer|-parser <filepath>  OR  <filepath> [-o <output.class>]");
                System.exit(1);
            }

            try (FileReader reader = new FileReader(filePath)) {
                Lexer lexer = new Lexer(reader);

                if (mode.equals("-lexer")) {
                    Symbol symbol = lexer.getNextSymbol();
                    while (symbol.getType() != TokenType.EOF) {
                        System.out.println(symbol);
                        symbol = lexer.getNextSymbol();
                    }

                } else {
                    Parser parser = new Parser(lexer);
                    ProgramNode root = parser.getAST();
                    SymbolTable table = new SymbolTable();
                    root.checkSemantics(table);
                    root.print(0);
                    System.out.println("Semantic Analysis complete with success !");
                }

            } catch (IOException e) {
                System.err.println("File error: " + e.getMessage());
                System.exit(1);
            } catch (RuntimeException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
            return;
        }

        // error print
        System.err.println("Usage:");
        System.err.println("  <source.lang>                  (code generation, output to test.class)");
        System.err.println("  <source.lang> -o <out.class>   (code generation, custom output)");
        System.err.println("  -lexer  <source.lang>          (lexer debug)");
        System.err.println("  -parser <source.lang>          (parser + semantic debug)");
        System.exit(1);

    }

    // from Lexer to code generation
    private static void runCodeGen(String sourceFile, String outputFile) {
        try (FileReader reader = new FileReader(sourceFile)) {

            // Lexer
            Lexer lexer = new Lexer(reader);

            // Parser
            Parser parser = new Parser(lexer);
            ProgramNode root = parser.getAST();

            // Semantic analysis
            SymbolTable table = new SymbolTable();
            root.checkSemantics(table);

            // Code generation
            Codegenerator cg = new Codegenerator(outputFile);
            root.generateCode(cg);

        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}