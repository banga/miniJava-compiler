package miniJava.SyntacticAnalyzer;

/**
 * An enum augmented with extra information such as spelling and source position
 */
public enum TokenType {

	NUMBER("number"),
	BINARY_OPERATOR("binary operator"),
	UNARY_OPERATOR("unary operator"),

	LPAREN("("),
	RPAREN(")"),
	LSQUARE("["),
	RSQUARE("]"),
	LCURL("{"),
	RCURL("}"),
	COMMA(","),
	SEMICOLON(";"),
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

	EOT("end of text"); 

	public final String spelling;

	TokenType(String spelling) {
		this.spelling = spelling;
	}
}
