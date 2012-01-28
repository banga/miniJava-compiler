package miniJava.SyntacticAnalyzer;

@SuppressWarnings("serial")
public class SyntaxErrorException extends Exception {
	public Token token = null;
	public SourcePosition position = null;
	public String extra = "Syntax error";

	public SyntaxErrorException(Token token, SourcePosition position) {
		this.token = token;
		this.position = position;
	}

	public SyntaxErrorException(Token token, SourcePosition position, String extra) {
		this.token = token;
		this.position = position;
		this.extra = extra;
	}
	
	public String toString() {
		return "Syntax error: " + extra + " at " + token.spelling  + ", " + position;
	}
}
