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
        if (expression == null) return "void";
        String exprType = expression.checkSemantics(table);

        if (operator.equals("MINUS") || operator.equals("-")) {
            if (!SymbolTable.typesAreCompatible(exprType, "int")
                    && !SymbolTable.typesAreCompatible(exprType, "float")) {
                SymbolTable.crash("OperatorError",
                        "unary minus requires int or float but received: " + exprType);
            }
            return exprType != null ? exprType : "void";
        }
        if (operator.equals("NOT") || operator.equals("not")) {
            if (!SymbolTable.typesAreCompatible(exprType, "bool")) {
                SymbolTable.crash("OperatorError",
                        "unary not requires bool but received: " + exprType);
            }
            return "bool";
        }
        return exprType != null ? exprType : "void";
    }
}
