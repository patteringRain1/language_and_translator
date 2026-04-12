package compiler.AST.declaration;

import compiler.AST.basic.ASTnode;
import compiler.Semantic.SymbolTable;
import java.util.List;

// for the declaration of function
public class functiondeclarationnode extends ASTnode {
    // function returntype
    private String returnType;
    // function name
    private String name;
    // the parameter type list
    private List<String> paramTypes;
    // the parameter name list
    private List<String> paramNames;
    // function body
    private ASTnode body;

    public functiondeclarationnode(String returnType, String name, List<String> paramTypes, List<String> paramNames, ASTnode body) {
        this.returnType = returnType;
        this.name = name;
        this.paramTypes = paramTypes;
        this.paramNames = paramNames;
        this.body = body;
    }

    // print the structure of function declaration
    public void print(int indent) {
        StringBuilder paramStr = new StringBuilder("(");
        int k = paramNames.size();
        for (int i = 0; i < k; i++) {
            paramStr.append(paramTypes.get(i)).append(" ").append(paramNames.get(i));
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

    @Override
    public String checkSemantics(SymbolTable table) {
        table.declareFunction(this.name, this.returnType.toLowerCase(), this.paramTypes);

        table.enterScope();
        for (int i = 0; i < paramNames.size(); i++) {
            String type = paramTypes.get(i);
            String name = paramNames.get(i);
            table.declareVariable(name, type);
        }

        String previousType = table.getCurrentReturnType();
        table.setCurrentReturnType(this.returnType.toLowerCase());

        if (body != null) {
            body.checkSemantics(table);
        }

        table.setCurrentReturnType(previousType);
        table.exitScope();
        return "void";
    }
}
