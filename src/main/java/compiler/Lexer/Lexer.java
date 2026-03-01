package compiler.Lexer;
import java.io.IOException;
import java.io.Reader;

public class Lexer {
    private Reader reader;
    private int currentChar;

    public Lexer(Reader input) {
        this.reader = input;
        nextChar();
    }

    private void nextChar(){
        try {
            this.currentChar = reader.read();
        } catch(IOException e) {
            this.currentChar = -1;
        }
    }

    private void ignoreCommentsAndWhitespace() {
        while (true) {
            if (Character.isWhitespace(currentChar)) {
                nextChar();
            } else if (currentChar == '#') {
                while (currentChar != '\n' && currentChar != '\r' && currentChar != -1) {
                    nextChar();
                }
            }
            else {
                break;
            }
        }
    }

    /** handle keyword, identifier base types and collections **/
    private Symbol handleWord() {
        StringBuilder sb = new StringBuilder();
        char firstChar = (char) currentChar;

        while (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
            sb.append((char) currentChar);
            nextChar();
        }

        String word = sb.toString();
        String lowerWord = word.toLowerCase();

        switch (lowerWord) {
            case "final" : return new Symbol(TokenType.FINAL);
            case "coll" : return new Symbol(TokenType.COLL);
            case "def":    return new Symbol(TokenType.DEF);
            case "for":    return new Symbol(TokenType.FOR);
            case "while":  return new Symbol(TokenType.WHILE);
            case "if":     return new Symbol(TokenType.IF);
            case "else":   return new Symbol(TokenType.ELSE);
            case "return": return new Symbol(TokenType.RETURN);
            case "not":    return new Symbol(TokenType.NOT);
            case "array":  return new Symbol(TokenType.ARRAY);
            case "true":   return new Symbol(TokenType.BOOLEAN_VALUE, true);
            case "false":  return new Symbol(TokenType.BOOLEAN_VALUE, false);
            case "int":    return new Symbol(TokenType.INT);
            case "float":  return new Symbol(TokenType.FLOAT);
            case "string": return new Symbol(TokenType.STRING);
            case "bool":   return new Symbol(TokenType.BOOL);
        }

        if (Character.isUpperCase(firstChar)) {
            return new Symbol(TokenType.COLLECTION, word);
        } else {
            return new Symbol(TokenType.IDENTIFIER, word);
        }
    }

    private Symbol handleNumber(boolean startedWithDot) {
        StringBuilder sb = new StringBuilder();
        boolean hasDecimalPoint = startedWithDot;

        if (startedWithDot) {
            sb.append("0.");
        }

        while (Character.isDigit(currentChar) || (currentChar == '.' && !hasDecimalPoint)) {
            if (currentChar == '.') {
                hasDecimalPoint = true;
                sb.append('.');
            } else {
                sb.append((char) currentChar);
            }
            nextChar();
        }

        String result = sb.toString();

        if (hasDecimalPoint) {
            return new Symbol(TokenType.FLOAT_VALUE, Float.parseFloat(result));
        } else {
            return new Symbol(TokenType.INT_VALUE, Integer.parseInt(result));
        }
    }

    private Symbol handleString() {
        StringBuilder sb = new StringBuilder();
        nextChar();

        while (currentChar != '"' && currentChar != -1) {
            if (currentChar == '\\') {
                nextChar();
                switch (currentChar) {
                    case 'n' : sb.append(('\n')); break;
                    case '\\' : sb.append(('\\')); break;
                    case '"' : sb.append('"'); break;
                    default:
                        throw new RuntimeException("string : invalid sequence" + (char)currentChar);
                }
            } else {
                sb.append((char) currentChar);
            }
            nextChar();
        }
        if (currentChar == -1) {
            throw new RuntimeException("string : lexical error");
        }
        nextChar();
        return new Symbol(TokenType.STRING_VALUE, sb.toString());
    }

    private Symbol handleSpecialSymbols() {
        int first = currentChar;
        nextChar();

        switch(first) {
            case '=' :
                if (currentChar == '=') {
                    nextChar();
                    return new Symbol(TokenType.EQUALS);
                } else if (currentChar == '/') {
                    nextChar();
                    if (currentChar == '=') {
                        nextChar();
                        return new Symbol(TokenType.NOT_EQUALS);
                    } else {
                        throw new RuntimeException("lexical error : special symbol");
                    }
                }
                return new Symbol(TokenType.ASSIGN);
            case '<' :
                if (currentChar == '=') {
                    nextChar();
                    return new Symbol(TokenType.LESS_THAN_OR_EQUALS);
                }
                return new Symbol(TokenType.LESS_THAN);
            case '>' :
                if (currentChar == '=') {
                    nextChar();
                    return new Symbol(TokenType.GREATER_THAN_OR_EQUALS);
                }
                return new Symbol(TokenType.GREATER_THAN);
            case '|':
                if (currentChar == '|') {
                    nextChar();
                    return new Symbol(TokenType.OR);
                }
                throw new RuntimeException("lexical error : special symbol, use || instead");
            case '&':
                if (currentChar == '&') {
                    nextChar();
                    return new Symbol(TokenType.AND);
                }
                throw new RuntimeException("lexical error : special symbol, use && instead");
            case '-' :
                if (currentChar == '>') {
                    nextChar();
                    return new Symbol(TokenType.RANGE);
                }
                return new Symbol(TokenType.MINUS);

            case '+': return new Symbol(TokenType.PLUS);
            case '*': return new Symbol(TokenType.TIMES);
            case '/': return new Symbol(TokenType.DIVIDE);
            case '%': return new Symbol(TokenType.MODULO);
            case '(': return new Symbol(TokenType.LEFT_PAREN);
            case ')': return new Symbol(TokenType.RIGHT_PAREN);
            case '{': return new Symbol(TokenType.LEFT_BRACE);
            case '}': return new Symbol(TokenType.RIGHT_BRACE);
            case '[': return new Symbol(TokenType.LEFT_BRACKET);
            case ']': return new Symbol(TokenType.RIGHT_BRACKET);
            case ';': return new Symbol(TokenType.SEMICOLON);
            case ',': return new Symbol(TokenType.COMMA);
            case '.': return new Symbol(TokenType.DOT);

            default :
                throw new RuntimeException("special symbol : unknown caracter " + (char) first);
        }
    }

    public Symbol getNextSymbol() {
        ignoreCommentsAndWhitespace();

        if (currentChar == -1) {
            return new Symbol(TokenType.EOF);
        }

        if (currentChar == '"') {
            return handleString();
        }

        if (Character.isDigit(currentChar)) {
            return handleNumber(false);
        }

        if (currentChar == '.') {
            nextChar();
            if(Character.isDigit(currentChar)) {
                return handleNumber(true);
            } else {
                return new Symbol(TokenType.DOT);
            }
        }

        if (Character.isLetter(currentChar) || currentChar == '_') {
            return handleWord();
        }

        return handleSpecialSymbols();
    }
}
