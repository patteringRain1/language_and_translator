package compiler.AST.basic;


import compiler.Semantic.SymbolTable;
import java.util.ArrayList;
import java.util.List;

// the root node
public class ProgramNode extends ASTnode {
    // base class in AST
    private List<ASTnode> declarations = new ArrayList<>();
    // parse AST node
    public void adddeclaration(ASTnode node) { declarations.add(node); }
    // print AST tree
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "Program");
        int k = declarations.size();
        for (int i = 0; i < k; i++) {
            ASTnode node = declarations.get(i);
            node.print(indent + 1);
        }
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        for (ASTnode declaration : declarations) {
            declaration.checkSemantics(table);
        }
        return "void";
    }
}
