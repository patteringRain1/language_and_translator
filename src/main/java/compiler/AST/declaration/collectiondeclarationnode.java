package compiler.AST.declaration;

import compiler.AST.basic.ASTnode;
import compiler.AST.expression.literalnode;
import compiler.Codegenerator.Codegenerator;
import compiler.Semantic.SymbolTable;
import java.util.ArrayList;
import java.util.List;


// for the declaration of collection
public class collectiondeclarationnode extends ASTnode {
    // the name of collection
    private String name;

    // for field inside the collection
    private List<ASTnode> fields = new ArrayList<>();
    public collectiondeclarationnode(String name) {
        this.name = name;
    }
    // add field to collection
    public void addfield(ASTnode field){
        fields.add(field);
    }
    // print the structure of collection declaration
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "collection, " + name);
        int k = fields.size();
        for(int i = 0; i < k; i++){
            ASTnode field = fields.get(i);
            field.print(indent + 1);
        }
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        if (!Character.isUpperCase(this.name.charAt(0))) {
            SymbolTable.crash("CollectionError", "the name of collections " + this.name + " must start with a uppercase");
        }

        table.declareCollection(this.name, this.fields);
        return "void";
    }

    @Override
    public void generateCode(Codegenerator cg) {
        // collection is created as a separate class in JVM
        cg.beginCollectionClass(this.name);
        int k = fields.size();
        for (int i = 0; i < k; i++) {
            ASTnode field = fields.get(i);
            if (field instanceof literalnode) {
                literalnode f = (literalnode) field;
                String fieldName = (String) f.getValue();
                String fieldType = f.getLiteralType().replace("Field: ", "");
                cg.addCollectionField(fieldName, fieldType);
            }
        }
        // receives all fields as parameters
        cg.endCollectionClass(this.name, this.fields);
    }
}
