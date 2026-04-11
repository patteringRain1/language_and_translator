package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;
import compiler.Semantic.SymbolTable;

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

    @Override
    public String checkSemantics(SymbolTable table) {
        String targetType = target.checkSemantics(table);
        String valueType = value.checkSemantics(table);

        if (!targetType.equalsIgnoreCase(valueType)) {
            SymbolTable.crash("typeError", "the target has type of " + targetType + " but assigned with " + valueType);
        }
        return "void";
    }
}
