package compiler.Semantic;

import compiler.AST.basic.ASTnode;
import compiler.AST.expression.literalnode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
  private Stack<Map<String, String>> scopes;
  private String currentReturnType = null;
  private Map<String, Map<String, String>> collectionDefinitions = new HashMap<>();

  public SymbolTable() {
    this.scopes = new Stack<>();
    enterScope();
  }

  public void enterScope() {
    scopes.push(new HashMap<>());
  }

  public void exitScope() {
    scopes.pop();
  }

  // when we declare a new variable
  public void declareVariable(String name, String type) {
    if (scopes.peek().containsKey(name)) {
      crash("scopeError", "variable " + name + " is already declared in this scope");
    }
    scopes.peek().put(name, type);
  }

  public void declareCollection(String name, List<ASTnode> fields) {
    if (collectionDefinitions.containsKey(name)) {
      crash("collectionError", "the collection " + name + " is already defined");
    }

    Map<String, String> fieldMap = new HashMap<>();

    for (ASTnode node : fields) {
      if (node instanceof literalnode) {
        literalnode fieldNode = (literalnode) node;
        String fieldName = (String) fieldNode.getValue();
        String fieldType = fieldNode.getLiteralType().replace("Field: ", "");

        if (fieldMap.containsKey(fieldName)) {
          crash("collectionError", "field " + fieldName + "doubled in " + name);
        }
        fieldMap.put(fieldName, fieldType);
      }
    }
    collectionDefinitions.put(name, fieldMap);
  }

  // give the type of a variable
  public String giveVariableType(String name) {
    for (int i = scopes.size()-1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name)) {
        return scopes.get(i).get(name);
      }
    }
    crash("scopeError", "variable " + name + " is not declared");
    return null;
  }

  public static void crash(String errorType, String message) {
    System.out.println(errorType + " : " + message);
    System.exit(2);
  }

  public String getCurrentReturnType() {
    return currentReturnType;
  }

  public void setCurrentReturnType(String currentReturnType) {
    this.currentReturnType = currentReturnType;
  }
}
