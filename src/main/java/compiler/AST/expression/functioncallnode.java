package compiler.AST.expression;

import compiler.AST.basic.ASTnode;
import compiler.Semantic.SymbolTable;
import java.util.ArrayList;
import java.util.List;

// for function call expression
public class functioncallnode extends ASTnode {
    // function name
    private String functionname;
    // arguments list
    private List<ASTnode> arguments = new ArrayList<>();

    public functioncallnode(String functionname) {
        this.functionname = functionname;
    }

    // add argument to an function
    public void addargument(ASTnode arg){
        this.arguments.add(arg);
    }

    // print the structure of function call expression
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "function call expression");
        int k = arguments.size();
        for(int i = 0; i < k; i++){
            ASTnode arg = arguments.get(i);
            if(arg != null){
                arg.print(indent + 1);
            }
        }
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        return null;
    }
}
