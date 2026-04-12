package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;
import compiler.Semantic.SymbolTable;

// for while loop statement
public class whilenode extends ASTnode {
    // the condition that determines whether the loop continues
    private ASTnode condition;
    // the block of loop
    private ASTnode body;

    public whilenode(ASTnode condition, ASTnode body) {
        this.condition = condition;
        this.body = body;
    }

    // print the structure of while loop statement
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "while statement");
        if(condition != null) {
            condition.print(indent + 1);
        }
        if(body != null) {
            body.print(indent + 1);
        }
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        String condType = this.condition.checkSemantics(table);
        if (condType == null || !condType.equalsIgnoreCase("bool")) {
            SymbolTable.crash("MissingConditionError", "the condition of a while must be a bool but received : " + condType);
        }
        table.enterScope();
        this.body.checkSemantics(table);
        table.exitScope();
        return "void";
    }
}
