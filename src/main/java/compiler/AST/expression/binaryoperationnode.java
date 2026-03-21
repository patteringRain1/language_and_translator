package compiler.AST.expression;

import compiler.AST.basic.ASTnode;

// for binary operation expression
public class binaryoperationnode extends ASTnode {
    // binary operator
    private String operator;
    // left operand
    private ASTnode left;
    // right operand
    private ASTnode right;

    public binaryoperationnode(String operator, ASTnode left, ASTnode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    // print the structure of binary operation expression
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "binary operation: " + operator);
        if(left != null){
            left.print(indent + 1);
        }
        if(right != null){
            right.print(indent + 1);
        }
    }
}
