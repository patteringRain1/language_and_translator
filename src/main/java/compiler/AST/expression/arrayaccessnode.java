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

    // let it return correct format
    @Override
    public String toString() {
        return this.arrayname + "[" + this.indexexpression.toString() + "]";
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        if (arrayname.equalsIgnoreCase("int") || arrayname.equalsIgnoreCase("float")
                || arrayname.equalsIgnoreCase("string") || arrayname.equalsIgnoreCase("bool")) {
            if (indexexpression != null) {
                indexexpression.checkSemantics(table);
            }
            return arrayname.toLowerCase() + "[]";
        }
        String arrayType = table.giveVariableType(this.arrayname);

        String indexType = this.indexexpression.checkSemantics(table);
        if (!SymbolTable.typesAreCompatible(indexType, "int")) {
            SymbolTable.crash("OperatorError", "array index must be int but received: " + indexType);
        }

        if (arrayType != null && arrayType.endsWith("[]")) {
            return arrayType.replace("[]", "");
        }

        SymbolTable.crash("TypeError", this.arrayname + " is not an array.");
        return "void";
    }
}
