package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.SourcePosition;

/**
 * Represents a token by its type, spelling and position
 */
public class Token {
	public TokenType type;
	public String spelling;
	public SourcePosition position;

	public Token(TokenType type, String spelling, SourcePosition position) {
		this.type = type;
		this.spelling = spelling;
		this.position = new SourcePosition(position.line, position.column);
	}

	/**
	 * Create token from given spelling by matching with keywords or literals.
	 * If no match is found, the type is set to IDENTIFIER
	 * 
	 * @param spelling
	 * @param position
	 */
	public Token(String spelling, SourcePosition position) {
		boolean matched = false;

		// Use Enum.values() method to get all token types
		for (TokenType t : TokenType.values()) {
			if (t.spelling != null && (t.spelling.compareTo(spelling) == 0)) {
				this.type = t;
				matched = true;
				break;
			}
		}

		if (!matched)
			type = TokenType.IDENTIFIER;

		this.spelling = spelling;
		this.position = new SourcePosition(position.line, position.column);
	}

	public String toString() {
		return type + ": " + spelling + " at " + position;
	}
}
