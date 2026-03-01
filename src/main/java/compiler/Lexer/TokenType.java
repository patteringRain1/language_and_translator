package compiler.Lexer;

public enum TokenType {
  IDENTIFIER, //they must start with a non-capital letter or underscore
  COLLECTION, // must begin with a capital letter

  /** keywords**/
  FINAL, COLL, DEF, FOR, WHILE, IF, ELSE, RETURN, NOT, ARRAY,

  /** base types**/
  INT, FLOAT, STRING, BOOL,

  /** values **/
  INT_VALUE, // 324
  FLOAT_VALUE, // 0.234
  STRING_VALUE, // "hello"
  BOOLEAN_VALUE, // true and false

  /** special symbols **/
  ASSIGN, // =
  PLUS, // +
  MINUS, // -
  TIMES, // *
  DIVIDE, // /
  MODULO, // %
  EQUALS, // ==
  NOT_EQUALS, // =/=
  LESS_THAN, // <
  GREATER_THAN, // >
  LESS_THAN_OR_EQUALS, // <=
  GREATER_THAN_OR_EQUALS, // >=
  LEFT_PAREN, // (
  RIGHT_PAREN, // )
  LEFT_BRACE, // {
  RIGHT_BRACE, // }
  LEFT_BRACKET, // [
  RIGHT_BRACKET, // ]
  DOT, // .
  AND, // &&
  OR, // ||
  SEMICOLON, // ;
  COMMA, // ,
  RANGE, // ->

  /** fin de fichier **/
  EOF
}
