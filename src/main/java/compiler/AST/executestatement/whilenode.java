package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;

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
}
