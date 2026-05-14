package compiler.AST.expression;

import compiler.AST.basic.ASTnode;
import compiler.Codegenerator.Codegenerator;
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

            if (!SymbolTable.typesAreCompatible(leftType, rightType)) {
                SymbolTable.crash("OperatorError",
                        leftType + " cannot operate with " + rightType);
            }
            //"+" is legal for String, other operators are not
            boolean isPlus = operator.equals("PLUS") || operator.equals("+");
            if (!isPlus) {
                if (!SymbolTable.typesAreCompatible(leftType, "int")
                        && !SymbolTable.typesAreCompatible(leftType, "float")) {
                    SymbolTable.crash("OperatorError",
                            leftType + " is not a number.");
                }
            } else {
                if (!SymbolTable.typesAreCompatible(leftType, "int")
                        && !SymbolTable.typesAreCompatible(leftType, "float")
                        && !SymbolTable.typesAreCompatible(leftType, "string")) {
                    SymbolTable.crash("OperatorError",
                            leftType + " does not support + operator.");
                }
            }
            return leftType != null ? leftType : "void";
        }

        if (operator.equals("EQUALS") || operator.equals("==") ||
            operator.equals("NOT_EQUALS") || operator.equals("=/=") ||
            operator.equals("LESS_THAN") || operator.equals("<") ||
            operator.equals("LESS_THAN_OR_EQUALS") || operator.equals("<=") ||
            operator.equals("GREATER_THAN") || operator.equals(">") ||
            operator.equals("GREATER_THAN_OR_EQUALS") || operator.equals(">=")) {

            if (!SymbolTable.typesAreCompatible(leftType, rightType)) {
                SymbolTable.crash("OperatorError", "Comparaison impossible entre " + leftType + " et " + rightType);
            }
            return "bool";
        }

        if (operator.equals("AND") || operator.equals("&&") ||
            operator.equals("OR") || operator.equals("||")) {

            if (!SymbolTable.typesAreCompatible(leftType, "bool") || !SymbolTable.typesAreCompatible(rightType, "bool")) {
                SymbolTable.crash("OperatorError", "logical operators require bool.");
            }
            return "bool";
        }

        return "void";
    }

    @Override
    public void generateCode(Codegenerator cg) {
        // left operand and right operand
        left.generateCode(cg);
        right.generateCode(cg);

        String op = (operator != null) ? operator : "";
        String leftType = cg.getExprType(left);

        // binary operation
        if (op.equals("PLUS") || op.equals("+")) {
            if ("string".equalsIgnoreCase(leftType)) {
                cg.emitStringConcat();
            } else if ("float".equalsIgnoreCase(leftType)) {
                cg.emitInstruction(org.objectweb.asm.Opcodes.FADD);
            } else {
                cg.emitInstruction(org.objectweb.asm.Opcodes.IADD);
            }
        } else if (op.equals("MINUS") || op.equals("-")) {
            if ("float".equalsIgnoreCase(leftType)) {
                cg.emitInstruction(org.objectweb.asm.Opcodes.FSUB);
            } else {
                cg.emitInstruction(org.objectweb.asm.Opcodes.ISUB);
            }
        } else if (op.equals("TIMES") || op.equals("*")) {
            if ("float".equalsIgnoreCase(leftType)) {
                cg.emitInstruction(org.objectweb.asm.Opcodes.FMUL);
            } else {
                cg.emitInstruction(org.objectweb.asm.Opcodes.IMUL);
            }
        } else if (op.equals("DIVIDE") || op.equals("/")) {
            if ("float".equalsIgnoreCase(leftType)) {
                cg.emitInstruction(org.objectweb.asm.Opcodes.FDIV);
            } else {
                cg.emitInstruction(org.objectweb.asm.Opcodes.IDIV);
            }
        } else if (op.equals("MODULO") || op.equals("%")) {
            cg.emitInstruction(org.objectweb.asm.Opcodes.IREM);
        }
        // comparison operator
        else if (op.equals("EQUALS") || op.equals("==")) {
            cg.emitComparison("==", leftType);
        } else if (op.equals("NOT_EQUALS") || op.equals("=/=")) {
            cg.emitComparison("!=", leftType);
        } else if (op.equals("LESS_THAN") || op.equals("<")) {
            cg.emitComparison("<", leftType);
        } else if (op.equals("LESS_THAN_OR_EQUALS") || op.equals("<=")) {
            cg.emitComparison("<=", leftType);
        } else if (op.equals("GREATER_THAN") || op.equals(">")) {
            cg.emitComparison(">", leftType);
        } else if (op.equals("GREATER_THAN_OR_EQUALS") || op.equals(">=")) {
            cg.emitComparison(">=", leftType);
        }
        // logistic operator
        else if (op.equals("AND") || op.equals("&&")) {
            cg.emitInstruction(org.objectweb.asm.Opcodes.IAND);
        } else if (op.equals("OR") || op.equals("||")) {
            cg.emitInstruction(org.objectweb.asm.Opcodes.IOR);
        }
    }
}