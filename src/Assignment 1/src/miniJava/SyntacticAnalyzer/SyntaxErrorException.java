package miniJava.SyntacticAnalyzer;

@SuppressWarnings("serial")
public class SyntaxErrorException extends Exception {
	public Token token = null;
	public String extra = "Syntax error";

	public SyntaxErrorException(Token token) {
		this.token = token;
	}

	public SyntaxErrorException(Token token, String extra) {
		this.token = token;
		this.extra = extra;
	}

	public String toString() {
		return "Syntax error: " + extra + " at " + token.spelling + ", " + token.position;
	}
}
