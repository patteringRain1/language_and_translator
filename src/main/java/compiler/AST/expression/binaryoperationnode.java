package compiler.AST.expression;

import compiler.AST.basic.ASTnode;
import compiler.Semantic.SymbolTable;

public class binaryoperationnode extends ASTnode {
    private String operator;
    private ASTnode left;
    private ASTnode right;

    public binaryoperationnode(String operator, ASTnode left, ASTnode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "binary operation: " + operator);
        if(left != null) left.print(indent + 1);
        if(right != null) right.print(indent + 1);
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        String leftType = left.checkSemantics(table);
        String rightType = right.checkSemantics(table);

        if (operator.equals("PLUS") || operator.equals("+") ||
            operator.equals("MINUS") || operator.equals("-") ||
            operator.equals("TIMES") || operator.equals("*") ||
            operator.equals("DIVIDE") || operator.equals("/") ||
            operator.equals("MODULO") || operator.equals("%")) {

            if (!leftType.equalsIgnoreCase(rightType)) {
                SymbolTable.crash("OperatorError", leftType + " ne peut pas operer avec " + rightType);
            }
            if (!leftType.equalsIgnoreCase("int") && !leftType.equalsIgnoreCase("float")) {
                SymbolTable.crash("OperatorError", leftType + " n'est pas un nombre.");
            }
            return leftType;
        }

        if (operator.equals("EQUALS") || operator.equals("==") ||
            operator.equals("NOT_EQUALS") || operator.equals("=/=") ||
            operator.equals("LESS_THAN") || operator.equals("<") ||
            operator.equals("LESS_THAN_OR_EQUALS") || operator.equals("<=") ||
            operator.equals("GREATER_THAN") || operator.equals(">") ||
            operator.equals("GREATER_THAN_OR_EQUALS") || operator.equals(">=")) {

            if (!leftType.equalsIgnoreCase(rightType)) {
                SymbolTable.crash("OperatorError", "Comparaison impossible entre " + leftType + " et " + rightType);
            }
            return "bool";
        }

        if (operator.equals("AND") || operator.equals("&&") ||
            operator.equals("OR") || operator.equals("||")) {

            if (!leftType.equalsIgnoreCase("bool") || !rightType.equalsIgnoreCase("bool")) {
                SymbolTable.crash("OperatorError", "Les operateurs logiques attendent des bool.");
            }
            return "bool";
        }

        System.out.println(">>> FATAL DEBUG: Opérateur inconnu -> [" + operator + "]");
        return "void";
    }
}