package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;

// assignmenr statement
public class assignmentnode extends ASTnode {
    // assignment target
    private ASTnode target;
    // assignment value
    private ASTnode value;

    public assignmentnode(ASTnode target, ASTnode value) {
        this.target = target;
        this.value = value;
    }

    // print the structure of assignment statement
    public void print(int indent) {
        System.out.println(getIndent(indent) + "assignment");
        if(target != null){
            target.print(indent + 1);
        }
        if(value != null){
            value.print(indent + 1);
        }
    }
}
