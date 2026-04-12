package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;
import compiler.Semantic.SymbolTable;

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

    @Override
    public String checkSemantics(SymbolTable table) {
        String expectedType = table.getCurrentReturnType();
        String actualType = "void";

        if (this.value != null){
            actualType = this.value.checkSemantics(table);
        }

        if (!SymbolTable.typesAreCompatible(expectedType, actualType)) {
            SymbolTable.crash("ReturnError", "waiting for a return type of " + expectedType + " but received " + actualType);
        }
        return "void";
    }
}
