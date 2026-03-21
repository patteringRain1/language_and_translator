package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;

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
}
