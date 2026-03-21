package compiler.AST.declaration;

import compiler.AST.basic.ASTnode;
import java.util.List;

// for the declaration of function
public class functiondeclarationnode extends ASTnode {
    // fuction name
    private String name;
    // the parameter list
    private List<String> params;
    // function body
    private ASTnode body;

    public functiondeclarationnode(String name, List<String> params, ASTnode body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    // print the structure of function declaration
    public void print(int indent) {
        StringBuilder paramStr = new StringBuilder("(");
        int k = params.size();
        for (int i = 0; i < k; i++) {
            paramStr.append(params.get(i));
            if (i < k - 1) {
                paramStr.append(", ");
            }
        }
        paramStr.append(")");
        System.out.println(getIndent(indent) + "FunctionDefinition, " + name + paramStr.toString());
        if (body != null) {
            body.print(indent + 1);
        }
    }

}
