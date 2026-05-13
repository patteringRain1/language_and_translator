package compiler.AST.basic;

import compiler.Codegenerator.Codegenerator;
import compiler.Semantic.SymbolTable;

public abstract class ASTnode {
    // print
    public abstract void print(int indent);
    // get indent
    protected String getIndent(int indent) {
        return " ".repeat(indent);
    }
    // each node can return its type (INT, BOOLEAN, ...)
    public abstract String checkSemantics(SymbolTable table);
    // each node can generate code
    public abstract void generateCode(Codegenerator cg);
}
