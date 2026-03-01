package compiler.Lexer;

public class Symbol {
  private final TokenType type;
  private final Object value;

  // dans le cas où le symbole est un equals par exemple
  // et qu'il n'a pas de valeur
  public Symbol(TokenType type) {
    this.type = type;
    this.value = null;
  }

  // dans le cas où le symbole a une valeur
  public Symbol(TokenType type, Object value) {
    this.type = type;
    this.value = value;
  }

  @Override
  public String toString() {
    return "<" + type + ", " + value + ">";
  }

  public TokenType getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }
}
