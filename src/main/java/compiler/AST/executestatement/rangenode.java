package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;
import compiler.Codegenerator.Codegenerator;
import compiler.Semantic.SymbolTable;

public class rangenode extends ASTnode {
    private ASTnode start;
    private ASTnode end;

    public rangenode(ASTnode start, ASTnode end) {
      this.start = start;
      this.end = end;
    }

    @Override
    public void print(int indent) {
      System.out.println(getIndent(indent) + "range");
      start.print(indent + 1);
      end.print(indent + 1);
    }

    @Override
    public String checkSemantics(SymbolTable table) {

      this.start.checkSemantics(table);
      this.end.checkSemantics(table);
      return "void";

    }

  @Override
  public void generateCode(Codegenerator cg) {
    start.generateCode(cg);
    end.generateCode(cg);
  }

  // for fornode
  public void generateRangeStart(Codegenerator cg) {
    start.generateCode(cg);
  }

  public void generateRangeEnd(Codegenerator cg) {
    end.generateCode(cg);
  }
}