package compiler.AST.expression;

import compiler.AST.basic.ASTnode;

public class identifiernode extends ASTnode {
    // the name of identifier
    private String name;

    public identifiernode(String name) {
        this.name = name;
    }

    // print the information of identifier
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "Identifier, " + name);
    }

}
