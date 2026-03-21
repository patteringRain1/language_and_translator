package compiler.AST.expression;

import compiler.AST.basic.ASTnode;

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
}
