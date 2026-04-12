package compiler.AST.declaration;

import compiler.AST.basic.ASTnode;
import compiler.Lexer.Symbol;
import compiler.Semantic.SymbolTable;

// for declaration of variable
public class vardeclarationnode extends ASTnode {
    // the type of variable
    private String type;
    // the name of variable
    private String name;
    // whether the variable is final
    private boolean isfinal;
    // initial value of variable
    private ASTnode initialvalue;

    public vardeclarationnode(String type, String name, boolean isfinal, ASTnode initialvalue) {
        this.type = type;
        this.name = name;
        this.isfinal = isfinal;
        this.initialvalue = initialvalue;
    }


    // print the structure of the variable declaration .
    @Override
    public void print(int indent) {
        String prefix = isfinal? "final " : "";
        System.out.println(getIndent(indent) + prefix + "VariableDeclaration, " + type + " " + name);
        if (initialvalue != null) {
            initialvalue.print(indent + 1);
        }
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        if (!this.type.equals("existing_var")) {
            table.declareVariable(this.name, this.type);
        }

        if (this.initialvalue != null) {
            String typeValue = initialvalue.checkSemantics(table);
            if (!SymbolTable.typesAreCompatible(this.type, typeValue)) {
                SymbolTable.crash("TypeError", "variable " + name + "has type " + this.type + " but assigned with " + typeValue);
            }
        }
        return "void";
    }
}
