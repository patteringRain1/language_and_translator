package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;
import compiler.Lexer.Symbol;
import compiler.Semantic.SymbolTable;

public class ifelsenode extends ASTnode {
    // condition
    private ASTnode condition;
    // if block
    private ASTnode ifblock;
    // else block
    private ASTnode elseblock;

    public ifelsenode(ASTnode condition, ASTnode ifblock, ASTnode elseblock) {
        this.condition = condition;
        this.ifblock = ifblock;
        this.elseblock = elseblock;
    }


    // print if-else statement structure
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "if-else statement");
        if(condition != null) {
            condition.print(indent + 1);
        }
        if(ifblock != null) {
            elseblock.print(indent + 1);
        }
        if(elseblock != null) {
            System.out.println(getIndent(indent) + "else");
            elseblock.print(indent + 1);
        }
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        String condType = condition.checkSemantics(table);

        if (!condType.equals("BOOL")) {
            SymbolTable.crash("noConditionError", "condition of IF needs to be BOOL but received : " + condType);
        }

        table.enterScope();
        ifblock.checkSemantics(table);
        table.exitScope();

        if (elseblock != null) {
            table.enterScope();
            elseblock.checkSemantics(table);
            table.exitScope();
        }
        return "VOID";
    }
}
