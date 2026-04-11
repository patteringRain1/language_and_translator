package compiler.AST.expression;

import compiler.AST.basic.ASTnode;
import compiler.Semantic.SymbolTable;

// for literal constant
public class literalnode extends ASTnode {
    // the type of constant
    private String literalType;
    // the value of constant
    private Object value;

    public literalnode(String literalType, Object value) {
        this.literalType = literalType;
        this.value = value;
    }

    // print the information of literal constant
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + literalType + "_LITERAL, " + value);
    }

    public String getLiteralType() {
        return literalType;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        if (this.literalType.equals("identifier")) {
            return table.giveVariableType((String) this.value);
        }
        if (this.literalType.equals("int")) return "int";
        if (this.literalType.equals("float")) return "float";
        if (this.literalType.equals("string")) return "string";
        if (this.literalType.equals("bool")) return "bool";
        return "void";
    }
}
