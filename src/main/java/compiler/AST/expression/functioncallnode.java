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
        switch (functionname) {
            case "read_INT":
                return "int";
            case "read_FLOAT":
                return "float";
            case "read_STRING":
                return "string";
            case "length":
                return "int";
            case "floor":
                return "int";
            case "ceil":
                return "int";
            case "str":
                return "string";
            case "not":
                return "bool";
            case "print":
            case "println":
            case "print_INT":
            case "print_FLOAT": {
                for (ASTnode arg : arguments) {
                    if (arg != null) arg.checkSemantics(table);
                }
                return "void";
            }
            default: {
                List<String> expectedTypes = table.getFunctionParamTypes(functionname);
                int k = arguments.size();

                if (expectedTypes != null) {
                    if (k != expectedTypes.size()) {
                        SymbolTable.crash("ArgumentError",
                                "function " + functionname + " expects " + expectedTypes.size()
                                        + " arguments but received " + k);
                    }
                    for (int i = 0; i < k; i++) {
                        ASTnode arg = arguments.get(i);
                        if (arg != null) {
                            String actualType = arg.checkSemantics(table);
                            String expectedType = expectedTypes.get(i);
                            if (!SymbolTable.typesAreCompatible(actualType, expectedType)) {
                                SymbolTable.crash("ArgumentError",
                                        "argument " + (i+1) + " of function " + functionname
                                                + " expects " + expectedType
                                                + " but received " + actualType);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < k; i++) {
                        ASTnode arg = arguments.get(i);
                        if (arg != null) arg.checkSemantics(table);
                    }
                }

                String retType = table.getFunctionReturnType(functionname);
                return retType != null ? retType.toLowerCase() : functionname;
            }
        }
    }
}
