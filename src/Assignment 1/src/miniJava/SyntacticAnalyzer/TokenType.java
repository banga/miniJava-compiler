package miniJava.SyntacticAnalyzer;

public enum TokenType {
	SEMICOLON(";"),
	COMMA(","),
	LPAREN("("),
	RPAREN(")"),
	LSQUARE("["),
	RSQUARE("]"),
	LCURL("{"),
	RCURL("}"),
	LANGLE("<"),
	RANGLE(">"),
	EQUALTO("="),
	PLUS("+"),
	MINUS("-"),
	ASTERISK("*"),
	SLASH("/"),
	PIPE("|"),
	AMPERSAND("&"),
	BANG("!"),
	DOT("."),

	// Keywords
	CLASS("class"),
	PUBLIC("public"),
	PRIVATE("private"),
	RETURN("return"),
	STATIC("static"),
	INT("int"),
	BOOLEAN("boolean"),
	VOID("void"),
	THIS("this"),
	IF("if"),
	ELSE("else"),
	WHILE("while"),
	NEW("new"),
	TRUE("true"),
	FALSE("false"),

	NUMBER,
	IDENTIFIER,
	EOT; 

	public final String spelling;

	TokenType() {
		this.spelling = null;
	}

	TokenType(String spelling) {
		this.spelling = spelling;
	}
}
