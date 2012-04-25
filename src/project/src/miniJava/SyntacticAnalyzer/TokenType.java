package miniJava.SyntacticAnalyzer;

public enum TokenType {
	SEMICOLON(";"), COMMA(","), LPAREN("("), RPAREN(")"), LSQUARE("["), RSQUARE("]"), LCURL("{"), RCURL("}"), LANGLE(
			"<"), RANGLE(">"), EQUALTO("="), EQUALTO_EQUALTO("=="), LANGLE_EQUALTO("<="), RANGLE_EQUALTO(">="), BANG_EQUALTO(
			"!="), PLUS("+"), MINUS("-"), ASTERISK("*"), SLASH("/"), PIPE_PIPE("||"), AMPERSAND_AMPERSAND("&&"), BANG(
			"!"), DOT("."),

	// Keywords
	CLASS("class"), EXTENDS("extends"), PUBLIC("public"), PRIVATE("private"), RETURN("return"), STATIC("static"), INT(
			"int"), BOOLEAN("boolean"), VOID("void"), THIS("this"), IF("if"), ELSE("else"), WHILE("while"), NEW("new"), TRUE(
			"true"), FALSE("false"),

	NUMBER, IDENTIFIER, STRING, EOT;

	public final String spelling;

	TokenType() {
		this.spelling = null;
	}

	TokenType(String spelling) {
		this.spelling = spelling;
	}
}
