package compiler.AST.expression;

import compiler.AST.basic.ASTnode;
import compiler.Semantic.SymbolTable;

// for literal constant
public class literalnode extends ASTnode {
    // the type of constant
    private String literalType;
    // the value of constant
    private Object value;

    public literalnode(String literalType, Object value) {
        this.literalType = literalType;
        this.value = value;
    }

    // print the information of literal constant
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + literalType + "_LITERAL, " + value);
    }

    public String getLiteralType() {
        return literalType;
    }

    public Object getValue() {
        return value;
    }

    // make sure it return correct format
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        String lt = this.literalType.toLowerCase();
        // for identifier
        if (lt.equals("identifier")) {
            String varName = (String) this.value;
            String type = table.giveVariableType(varName);
            if (type == null) {
                SymbolTable.crash("ScopeError", "Variable " + varName + " is not defined");
            }
            return type;
        }

        //for field access
        if (lt.equals("fieldaccess")) {
            String fullName = (String) this.value;
            String[] parts = fullName.split("\\.");
            String firstPart = parts[0].split("\\[")[0];
            String currentType = table.giveVariableType(firstPart);

            if (currentType == null) {
                SymbolTable.crash("ScopeError", "Base object " + firstPart + " is not defined");
            }

            for (int i = 1; i < parts.length; i++) {
                String fieldName = parts[i];
                String lookupType = currentType.replace("[]", "");
                String nextType = table.getCollectionFieldType(lookupType, fieldName);

                if (nextType == null) {
                    SymbolTable.crash("TypeError", "Type '" + currentType + "' has no field '" + fieldName + "'");
                }
                currentType = nextType;
            }

            return currentType.toLowerCase();
        }

        if (lt.equals("integer") || lt.equals("int")) return "int";
        if (lt.equals("float"))                        return "float";
        if (lt.equals("string"))                       return "string";
        if (lt.equals("boolean") || lt.equals("bool")) return "bool";


        return "void";
    }
}
