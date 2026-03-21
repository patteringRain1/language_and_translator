package compiler.AST.expression;

import compiler.AST.basic.ASTnode;

// for index access expression of an array or collection
public class arrayaccessnode extends ASTnode {
    // array name
    private String arrayname;
    // index expression
    private ASTnode indexexpression;

    public arrayaccessnode(String arrayname, ASTnode indexexpression) {
        this.arrayname = arrayname;
        this.indexexpression = indexexpression;
    }

    // print the structure of array access
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "ArrayAccess, " + arrayname);
        if (indexexpression != null) {
            indexexpression.print(indent + 1);
        }
    }
}
