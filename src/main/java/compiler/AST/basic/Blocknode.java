package compiler.AST.basic;

import java.util.ArrayList;
import java.util.List;

// for the block of codeblock like if-else, while, for
public class Blocknode extends ASTnode {
    // for all statement node
    private List<ASTnode> statements = new ArrayList<>();

    // add new statements
    public void addstatement(ASTnode statement){statements.add(statement);}

    // print all the statements
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "Block");
        int k = statements.size();
        for (int i = 0; i < k; i++){
            ASTnode statement = statements.get(i);
            statement.print(indent + 1);
        }
    }
}
