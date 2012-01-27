package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.SourcePosition;

/**
 * Represents a token by its type, spelling and position
 */
public class Token {
	public final TokenType type;
	public final String spelling;
	public final SourcePosition position;

	public Token(TokenType type, String spelling, SourcePosition position) {
		this.type = type;
		this.spelling = spelling;
		this.position = position;
	}

	public boolean isKeyword() {
		switch (this.type) {
		case CLASS:
		case PUBLIC:
		case PRIVATE:
		case RETURN:
		case STATIC:
		case INT:
		case BOOLEAN:
		case VOID:
		case THIS:
		case IF:
		case ELSE:
		case WHILE:
		case NEW:
		case TRUE:
		case FALSE:
			return true;
		}
		return false;
	}
}
