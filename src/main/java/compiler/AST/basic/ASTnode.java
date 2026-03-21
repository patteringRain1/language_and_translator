package compiler.AST.basic;

public abstract class ASTnode {
    // print
    public abstract void print(int indent);
    // get indent
    protected String getIndent(int indent) {
        return " ".repeat(indent);
    }
}
