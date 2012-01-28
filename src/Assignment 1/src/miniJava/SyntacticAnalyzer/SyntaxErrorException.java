package miniJava.SyntacticAnalyzer;

@SuppressWarnings("serial")
public class SyntaxErrorException extends Exception {
	public Token token = null;
	public static final String ERROR_HEADER = "syntax error: ";

	public SyntaxErrorException(Token token) {
		super(ERROR_HEADER + "unexpected token " + token.spelling + " at " + token.position);
		this.token = token;
	}

	public SyntaxErrorException(String message) {
		super(ERROR_HEADER + message);
	}

	public SyntaxErrorException(String message, Token token) {
		super(ERROR_HEADER + message + " " + token.spelling + " at " + token.position);
		this.token = token;
	}
}
