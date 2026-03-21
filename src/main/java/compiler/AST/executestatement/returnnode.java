package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;

// for return statement of a function
public class returnnode extends ASTnode {
    // the expression of return, if it is null, it means function type is void
    private ASTnode value;

    public returnnode(ASTnode value) {
        this.value = value;
    }

    // print the structure of return statement
    public void print(int indent) {
        System.out.println(getIndent(indent) + "return statement");
        if(value != null) {
            value.print(indent + 1);
        }
    }
}
