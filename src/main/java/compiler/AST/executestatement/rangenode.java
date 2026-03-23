package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;

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
}