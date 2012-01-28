package miniJava.SyntacticAnalyzer;

@SuppressWarnings("serial")
public class SyntaxErrorException extends Exception {
	public String extra = "unxepected token ";
	public Token token = null;

	public SyntaxErrorException(Token token) {
		this.token = token;
	}

	public SyntaxErrorException(String extra, Token token) {
		this.extra = extra;
		this.token = token;
	}

	public String toString() {
		return "Syntax error: " + extra + " " + token.spelling + ", " + token.position;
	}
}
