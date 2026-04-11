package compiler.AST.expression;

import compiler.AST.basic.ASTnode;
import compiler.Semantic.SymbolTable;

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

    @Override
    public String checkSemantics(SymbolTable table) {
        String arrayType = table.giveVariableType(this.arrayname);

        String indexType = this.indexexpression.checkSemantics(table);
        if (!indexType.equalsIgnoreCase("int")) {
            SymbolTable.crash("OperatorError", "L'index d'un tableau doit être un int, reçu : " + indexType);
        }

        if (arrayType != null && arrayType.endsWith("[]")) {
            return arrayType.replace("[]", "");
        }

        SymbolTable.crash("TypeError", this.arrayname + " n'est pas un tableau.");
        return "void";
    }
}
