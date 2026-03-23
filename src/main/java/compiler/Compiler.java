package compiler;

import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Lexer.TokenType;
import compiler.Parser.Parser;
import compiler.AST.basic.ProgramNode;
import java.io.FileReader;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java main -lexer|-parser <filepath>");
            System.exit(1);
        }

        String mode = args[0];
        String filePath = args[1];

        if (!mode.equals("-lexer") && !mode.equals("-parser")) {
            System.err.println("Le mode doit être -lexer ou -parser");
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
            } else if (mode.equals("-parser")) {
                Parser parser = new Parser(lexer);
                ProgramNode root = parser.getAST();
                root.print(0);
            }

        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier : " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}