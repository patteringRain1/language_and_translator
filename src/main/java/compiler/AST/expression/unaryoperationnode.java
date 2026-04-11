package compiler.AST.expression;

import compiler.AST.basic.ASTnode;
import compiler.Semantic.SymbolTable;

// for binary operation expression
public class unaryoperationnode extends ASTnode {
    // unary operator
    private String operator;
    // operand expression
    private ASTnode expression;

    public unaryoperationnode(String operator, ASTnode expression) {
        this.operator = operator;
        this.expression = expression;
    }

    // print the structure of unary operation expression
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "unary operation: " + operator);
        if(expression != null){
            expression.print(indent + 1);
        }
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        return null;
    }
}
