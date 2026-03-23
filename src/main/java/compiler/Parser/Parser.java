package compiler.Parser;

import compiler.AST.basic.ASTnode;
import compiler.AST.basic.Blocknode;
import compiler.AST.basic.ProgramNode;
import compiler.AST.declaration.collectiondeclarationnode;
import compiler.AST.declaration.functiondeclarationnode;
import compiler.AST.declaration.vardeclarationnode;
import compiler.AST.executestatement.assignmentnode;
import compiler.AST.executestatement.fornode;
import compiler.AST.executestatement.ifelsenode;
import compiler.AST.executestatement.rangenode;
import compiler.AST.executestatement.returnnode;
import compiler.AST.executestatement.whilenode;
import compiler.AST.expression.arrayaccessnode;
import compiler.AST.expression.binaryoperationnode;
import compiler.AST.expression.functioncallnode;
import compiler.AST.expression.literalnode;
import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Lexer.TokenType;
import java.util.ArrayList;
import java.util.List;

public class Parser {
  private Lexer lexer;
  private Symbol currentSymbol;
  private Symbol nextSymbol;

  public Parser(Lexer lexer) {
    this.lexer = lexer;
    this.currentSymbol = lexer.getNextSymbol();
    this.nextSymbol = lexer.getNextSymbol();

  }

  // check if the token is the type we expected
  private void consume(TokenType expected) {
    if (currentSymbol.getType() == expected) {
      currentSymbol = nextSymbol;
      nextSymbol = lexer.getNextSymbol();
    } else {
      throw new RuntimeException("Syntaxe Error : expected " + expected
          + "reeived : " + currentSymbol.getType() + "with the value '" + currentSymbol.getValue() + "'");
    }
  }

  public ProgramNode getAST() {
    ProgramNode program = new ProgramNode();

    while (currentSymbol.getType() != TokenType.EOF) {
      program.adddeclaration(parseDeclaration());
    }

    return program;
  }

  // implemente the expression logic
  private ASTnode parseFactor() {
    TokenType type = currentSymbol.getType();
    Object val = currentSymbol.getValue();
    ASTnode node;

    if (type == TokenType.INT_VALUE) {
      consume(TokenType.INT_VALUE);
      node = new literalnode("Integer", val);
    } else if (type == TokenType.FLOAT_VALUE) {
      consume(TokenType.FLOAT_VALUE);
      node = new literalnode("Float", val);
    } else if (type == TokenType.STRING_VALUE) {
      consume(TokenType.STRING_VALUE);
      node = new literalnode("String", val);
    } else if (type == TokenType.BOOLEAN_VALUE) {
      consume(TokenType.BOOLEAN_VALUE);
      node = new literalnode("Boolean", val);
    } else if (type == TokenType.IDENTIFIER) {
      consume(TokenType.IDENTIFIER);
      node = new literalnode("Identifier", val);
    } else if (type == TokenType.COLLECTION || isBaseType(type)) {
      String typeName = parseType();
      node = parseFactorAfterType(typeName);
    } else if (type == TokenType.LEFT_PAREN) {
      consume(TokenType.LEFT_PAREN);
      node = parseExpression();
      consume(TokenType.RIGHT_PAREN);
    } else {
      throw new RuntimeException("expected expression but recieved : " + type);
    }

    while (true) {
      if (currentSymbol.getType() == TokenType.DOT) {
        consume(TokenType.DOT);
        String field = (String) currentSymbol.getValue();
        consume(TokenType.IDENTIFIER);
        node = new literalnode("FieldAccess", node.toString() + "." + field);
      } else if (currentSymbol.getType() == TokenType.LEFT_BRACKET) {
        consume(TokenType.LEFT_BRACKET);
        ASTnode index = parseExpression();
        consume(TokenType.RIGHT_BRACKET);
        node = new arrayaccessnode(node.toString(), index);
      } else if (currentSymbol.getType() == TokenType.LEFT_PAREN) {
        node = parseFunctionCall(node.toString());
      } else {
        break;
      }
    }
    return node;
  }

  private ASTnode parseTerm() {
    ASTnode left = parseFactor();

    while (currentSymbol.getType() == TokenType.TIMES || currentSymbol.getType() == TokenType.DIVIDE || currentSymbol.getType() == TokenType.MODULO) {
      String operation = (String) currentSymbol.getValue();
      TokenType type = currentSymbol.getType();
      consume(type);

      ASTnode right = parseFactor();
      left = new binaryoperationnode(operation, left, right);
    }
    return left;
  }

  private ASTnode parseAdditiveExpression() {
    ASTnode left = parseTerm();

    while (currentSymbol.getType() == TokenType.PLUS || currentSymbol.getType() == TokenType.MINUS) {
      String operation = (String) currentSymbol.getValue();
      TokenType type = currentSymbol.getType();
      consume(type);

      ASTnode right = parseTerm();
      left = new binaryoperationnode(operation, left, right);
    }
    return left;
  }

  private String parseType() {
    TokenType typeToken = currentSymbol.getType();

    if (!isBaseType(typeToken) && typeToken != TokenType.COLLECTION) {
      throw new RuntimeException("Expected type (INT, FLOAT, STRING, BOOL, or COLLECTION) but received: "
          + typeToken + " (value: " + currentSymbol.getValue() + ") "
          + "at position - currentSymbol was not a type token");
    }

    String typeName;
    if (typeToken == TokenType.COLLECTION) {
      typeName = (String) currentSymbol.getValue();
    } else {
      typeName = typeToken.toString().toLowerCase().replace("_value", "");
    }

    consume(typeToken);

    if (currentSymbol.getType() == TokenType.LEFT_BRACKET) {
      consume(TokenType.LEFT_BRACKET);
      consume(TokenType.RIGHT_BRACKET);
      typeName += "[]";
    }

    return typeName;
  }


  // the parser need to know which function to call when he reads a declaration
  private ASTnode parseDeclaration() {
    TokenType type = currentSymbol.getType();
    if (type == TokenType.FINAL) {
      return parseVarDeclaration(true);
    } else if (type == TokenType.COLL) {
      return parseCollectionDeclaration();
    } else if (type == TokenType.DEF) {
      return parseFunctionDeclaration();
    } else {
      return parseStatement();
    }
  }

  private boolean isBaseType(TokenType type) {
    return type == TokenType.INT || type == TokenType.FLOAT || type == TokenType.STRING || type == TokenType.BOOL;
  }

  private ASTnode parseVarDeclaration(boolean isFinal) {
    if (isFinal)
      consume(TokenType.FINAL);

    // INT, FLOAT ...
    String type = parseType();

    // name variable
    String name = (String) currentSymbol.getValue();
    consume(TokenType.IDENTIFIER);

    ASTnode initialValue = null;

    if (currentSymbol.getType() == TokenType.ASSIGN) {
      consume(TokenType.ASSIGN);
      initialValue = parseExpression();
    }
    consume(TokenType.SEMICOLON);

    return new vardeclarationnode(type, name, isFinal, initialValue);
  }

  private ASTnode parseCollectionDeclaration() {
    consume(TokenType.COLL);
    String name = (String) currentSymbol.getValue();
    consume(TokenType.COLLECTION);
    consume(TokenType.LEFT_BRACE);

    collectiondeclarationnode node = new collectiondeclarationnode(name);
    while (currentSymbol.getType() != TokenType.RIGHT_BRACE) {
      String type = parseType();

      String fieldName = (String) currentSymbol.getValue();
      consume(TokenType.IDENTIFIER);
      consume(TokenType.SEMICOLON);
      node.addfield(new literalnode("Field: " + type, fieldName));
    }
    consume(TokenType.RIGHT_BRACE);
    return node;
  }

  private List<String> parseParameters() {
    List<String> params = new ArrayList<>();
    consume(TokenType.LEFT_PAREN);

    if (currentSymbol.getType() != TokenType.RIGHT_PAREN) {
      do {
        parseType();

        String paramName = (String) currentSymbol.getValue();
        consume(TokenType.IDENTIFIER);
        params.add(paramName);

        if (currentSymbol.getType() == TokenType.COMMA) {
          consume(TokenType.COMMA);
        } else {
          break;
        }
      } while (true);
    }
    consume(TokenType.RIGHT_PAREN);
    return params;
  }

  private Blocknode parseBlock(){
    Blocknode block = new Blocknode();
    consume(TokenType.LEFT_BRACE);

    while (currentSymbol.getType() != TokenType.RIGHT_BRACE && currentSymbol.getType() != TokenType.EOF) {
      System.out.println("BLOCK LOOP TOKEN: " + currentSymbol.getType() + " " + currentSymbol.getValue());
      block.addstatement(parseStatement());
    }

    consume(TokenType.RIGHT_BRACE);
    return block;
  }

  private ASTnode parseFunctionDeclaration() {
    consume(TokenType.DEF);

    String returnType;
    String funcName;

    if (nextSymbol.getType() == TokenType.LEFT_PAREN) {
      returnType = "void";
      funcName = (String) currentSymbol.getValue();
      consume(TokenType.IDENTIFIER);
    } else {
      returnType = parseType();
      funcName = (String) currentSymbol.getValue();
      consume(TokenType.IDENTIFIER);
    }

    List<String> params = parseParameters();
    Blocknode body = parseBlock();
    return new functiondeclarationnode(funcName, params, body);
  }


  private ASTnode parseStatement() {
    System.out.println("DEBUG: current=" + currentSymbol.getType() +
        " next=" + nextSymbol.getType() +
        " value=" + currentSymbol.getValue());
    TokenType type = currentSymbol.getType();

    if (type == TokenType.FOR) { return parseForStatement(); }

    if (isDeclarationStart()) {
      return parseVarDeclaration(false);
    }

    if (type == TokenType.IF) { return parseIfStatement(); }
    else if (type == TokenType.WHILE) { return parseWhileStatement(); }
    else if (type == TokenType.RETURN) { return parseReturnStatement(); }
    else if (type == TokenType.LEFT_BRACE) { return parseBlock(); }
    else if (type == TokenType.SEMICOLON) {
      consume(TokenType.SEMICOLON);
      return null;
    }
    else if (type == TokenType.IDENTIFIER || type == TokenType.COLLECTION) {
      return parseAssignmentOrCall();
    }
    else {
      throw new RuntimeException("Syntax Error: unexpected token " + type +
          (currentSymbol.getValue() != null ? " with value '" + currentSymbol.getValue() + "'" : ""));
    }
  }

  private boolean isDeclarationStart() {
    TokenType type = currentSymbol.getType();

    if (!isBaseType(type) && type != TokenType.COLLECTION) {
      return false;
    }

    if (isBaseType(type)) {
      return nextSymbol.getType() == TokenType.IDENTIFIER
          || nextSymbol.getType() == TokenType.LEFT_BRACKET;
    } else {
      // COLLECTION
      return nextSymbol.getType() == TokenType.IDENTIFIER;
    }
  }

  private ASTnode parseIfStatement() {
    consume(TokenType.IF);
    consume(TokenType.LEFT_PAREN);
    ASTnode condition = parseExpression();
    consume(TokenType.RIGHT_PAREN);

    ASTnode thenBlock = parseBlock();
    ASTnode elseBlock = null;

    if (currentSymbol.getType() == TokenType.ELSE) {
      consume(TokenType.ELSE);
      elseBlock = parseBlock();
    }

    return new ifelsenode(condition, thenBlock, elseBlock);
  }

  private ASTnode parseWhileStatement() {
    consume(TokenType.WHILE);
    consume(TokenType.LEFT_PAREN);
    ASTnode condition = parseExpression();
    consume(TokenType.RIGHT_PAREN);
    Blocknode body = parseBlock();
    return new whilenode(condition, body);
  }

  private ASTnode parseForStatement() {
    consume(TokenType.FOR);
    consume(TokenType.LEFT_PAREN);

    String type = null;
    String name;

    // Si le token actuel est un type (INT, FLOAT) ou une COLLECTION
    if (isBaseType(currentSymbol.getType()) || currentSymbol.getType() == TokenType.COLLECTION) {
      type = parseType(); // Consomme le type (ex: INT)
      name = (String) currentSymbol.getValue();
      consume(TokenType.IDENTIFIER); // Consomme le nom (ex: i)
    } else {
      // Sinon, on considère que c'est une variable déjà déclarée
      name = (String) currentSymbol.getValue();
      consume(TokenType.IDENTIFIER); // Consomme directement 'i'
      type = "existing_var";
    }

    ASTnode init = new vardeclarationnode(type, name, false, null);

    consume(TokenType.SEMICOLON);

    ASTnode rangeStart = parseExpression();
    consume(TokenType.RANGE);
    ASTnode rangeEnd = parseExpression();
    ASTnode range = new rangenode(rangeStart, rangeEnd);

    consume(TokenType.SEMICOLON);

    ASTnode step = parseExpression();

    consume(TokenType.RIGHT_PAREN);
    ASTnode body = parseBlock();

    return new fornode(init, range, step, body);
  }

  private ASTnode parseReturnStatement() {
    consume(TokenType.RETURN);
    ASTnode expr = null;

    if (currentSymbol.getType() != TokenType.SEMICOLON) {
      expr = parseExpression();
    }
    consume(TokenType.SEMICOLON);
    return new returnnode(expr);
  }

  private ASTnode parseFunctionCall(String name) {
    functioncallnode node = new functioncallnode(name);
    consume(TokenType.LEFT_PAREN);

    if (currentSymbol.getType() != TokenType.RIGHT_PAREN) {
      do {
        node.addargument(parseExpression());
        if (currentSymbol.getType() == TokenType.COMMA) {
          consume(TokenType.COMMA);
        } else {
          break;
        }
      } while (true);
    }

    consume(TokenType.RIGHT_PAREN);
    return node;
  }

  private ASTnode parseAssignmentOrCall() {
    ASTnode target = parseFactor();

    if (currentSymbol.getType() == TokenType.ASSIGN) {
      consume(TokenType.ASSIGN);
      ASTnode value = parseExpression();
      consume(TokenType.SEMICOLON);
      return new assignmentnode(target, value);
    }

    if (target instanceof functioncallnode) {
      consume(TokenType.SEMICOLON);
      return target;
    }

    throw new RuntimeException("Invalid statement: expected assignment or function call");
  }

  private ASTnode parseFactorAfterType(String typeName) {
    if (currentSymbol.getType() == TokenType.LEFT_PAREN) {
      return parseFunctionCall(typeName);
    } else if (currentSymbol.getType() == TokenType.ARRAY) {
      consume(TokenType.ARRAY);
      consume(TokenType.LEFT_BRACKET);
      ASTnode size = parseExpression();
      consume(TokenType.RIGHT_BRACKET);
      return new arrayaccessnode(typeName, size);
    }
    return new literalnode("Type", typeName);
  }

  private ASTnode parseComparison() {
    ASTnode left = parseAdditiveExpression();

    while (currentSymbol.getType() == TokenType.EQUALS ||
        currentSymbol.getType() == TokenType.NOT_EQUALS ||
        currentSymbol.getType() == TokenType.LESS_THAN ||
        currentSymbol.getType() == TokenType.GREATER_THAN ||
        currentSymbol.getType() == TokenType.LESS_THAN_OR_EQUALS ||
        currentSymbol.getType() == TokenType.GREATER_THAN_OR_EQUALS) {

      if (nextSymbol.getType() == TokenType.RANGE) {
        break;
      }

      String op = (String) currentSymbol.getValue();
      if (op == null) op = currentSymbol.getType().toString();
      consume(currentSymbol.getType());

      ASTnode right = parseAdditiveExpression();
      left = new binaryoperationnode(op, left, right);
    }
    return left;
  }

  private ASTnode parseExpression() {
    ASTnode left = parseComparison();

    while (currentSymbol.getType() == TokenType.AND || currentSymbol.getType() == TokenType.OR) {
      String op = (String) currentSymbol.getValue();
      consume(currentSymbol.getType());

      ASTnode right = parseComparison();
      left = new binaryoperationnode(op, left, right);
    }
    return left;
  }
}
