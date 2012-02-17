package miniJava.SyntacticAnalyzer;

import java.io.InputStream;

public class Parser {
	private Scanner scanner;
	private Token currentToken = null;

	public Parser(InputStream in) {
		scanner = new Scanner(in);
	}

	/**
	 * Consumes the current token silently
	 * 
	 * @throws ScannerException
	 *             If a token cannot be constructed
	 */
	private void consume() throws SyntaxErrorException {
		try {
			currentToken = scanner.nextToken();
		} catch (ScannerException e) {
			// throw new SyntaxErrorException("unrecognized token ", new
			// Token(e.spelling, e.position));
			throw new SyntaxErrorException(e.getMessage());// , new
															// Token(e.spelling,
															// e.position));
		}
	}

	/**
	 * Consumes a token of expected type or throws an exception
	 * 
	 * @param type
	 *            expected token type
	 * @throws SyntaxErrorException
	 *             If an unexpected type is found
	 * @throws ScannerException
	 *             If a token cannot be constructed
	 */
	private void expect(TokenType type) throws SyntaxErrorException {
		if (currentToken.type != type)
			if (type.spelling != null) {
				throw new SyntaxErrorException("expected " + type.spelling + " but found", currentToken);
			} else {
				throw new SyntaxErrorException("expected " + type.toString() + " but found", currentToken);
			}

		consume();
	}

	/**
	 * Parses the <i>Program</i> non-terminal, which is also the start symbol of
	 * miniJava grammar
	 * 
	 * <pre>
	 * Program ::= (ClassDeclaration)* <b>eot</b>
	 * </pre>
	 * 
	 * @throws ScannerException
	 */
	public void parseProgram() throws SyntaxErrorException {
		consume();

		while (currentToken.type != TokenType.EOT)
			parseClassDeclaration();
	}

	/**
	 * Parses the <i>ClassDeclaration</i> non-terminal
	 * 
	 * <pre>
	 * ClassDeclaration ::= 
	 *     <b>class</b> <i>id</i> <b>{</b>
	 *         (FieldDeclaration | MethodDeclaration)*
	 *     <b>}</b>
	 * 
	 * FieldDeclaration ::= Declarators <i>id</i><b>;</b>
	 * 
	 * MethodDeclaration ::= 
	 *     Declarators <i>id</i><b>(</b>ParameterList?<b>) {</b>
	 *         Statement* (<b>return</b> Expression<b>;</b>)?
	 *     <b>}</b>
	 * 
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseClassDeclaration() throws SyntaxErrorException {
		expect(TokenType.CLASS);
		expect(TokenType.IDENTIFIER);
		expect(TokenType.LCURL);
		while (currentToken.type != TokenType.RCURL) {
			// Left factorize the Declarators and id
			parseDeclarators();
			expect(TokenType.IDENTIFIER);

			if (currentToken.type == TokenType.LPAREN) {
				// MethodDeclaration
				consume();
				if (currentToken.type != TokenType.RPAREN)
					parseParameterList();
				expect(TokenType.RPAREN);

				// Method body
				expect(TokenType.LCURL);
				while (currentToken.type != TokenType.RCURL) {
					if (currentToken.type == TokenType.RETURN) {
						consume();
						parseExpression();
						expect(TokenType.SEMICOLON);
						break;
					} else {
						parseStatement();
					}
				}
				expect(TokenType.RCURL);
			} else {
				expect(TokenType.SEMICOLON);
			}
		}
		expect(TokenType.RCURL);
	}

	/**
	 * Parses the <i>Declarators</i> non-terminal
	 * 
	 * <pre>
	 * Declarators ::= (<b>public</b> | <b>private</b>)? <b>static</b>? Type
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseDeclarators() throws SyntaxErrorException {
		if (currentToken.type == TokenType.PUBLIC || currentToken.type == TokenType.PRIVATE)
			consume();
		if (currentToken.type == TokenType.STATIC)
			consume();
		parseType();
	}

	/**
	 * Parses the <i>Type</i> non-terminal
	 * 
	 * <pre>
	 * Type ::= PrimType | ClassType | ArrayType
	 * PrimType ::= <b>int</b> | <b>boolean</b> | <b>void</b>
	 * ClassType ::= id
	 * ArrType ::= (int | ClassType) <b>[]</b>
	 * </pre>
	 * 
	 * <i>Type</i> can be rewritten as:
	 * 
	 * <pre>
	 * Type ::= (<b>int</b> | id) (<i>&#949;</i> | <b>[]</b>) | <b>boolean</b> | <b>void</b>
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseType() throws SyntaxErrorException {
		switch (currentToken.type) {
		case INT:
		case IDENTIFIER:
			consume();
			if (currentToken.type == TokenType.LSQUARE) {
				consume();
				expect(TokenType.RSQUARE);
			}
			break;
		case BOOLEAN:
		case VOID:
			consume();
			break;
		default:
			throw new SyntaxErrorException("expected a Type, found ", currentToken);
		}
	}

	/**
	 * Parses the <i>ParameterList</i> non-terminal
	 * 
	 * <pre>
	 * ParameterList ::= Type id (, Type id)*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseParameterList() throws SyntaxErrorException {
		parseType();
		expect(TokenType.IDENTIFIER);
		while (currentToken.type == TokenType.COMMA) {
			consume();
			parseType();
			expect(TokenType.IDENTIFIER);
		}
	}

	/**
	 * Parses the <i>ArgumentList</i> non-terminal
	 * 
	 * <pre>
	 * ArgumentList ::= Expression (, Expression)*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseArgumentList() throws SyntaxErrorException {
		parseExpression();
		while (currentToken.type == TokenType.COMMA) {
			consume();
			parseExpression();
		}
	}

	/**
	 * Parses the members of a <i>Reference</i>
	 * 
	 * <pre>
	 * Reference = (this | <b>id</b>) ReferenceMember
	 * ReferenceMember = (. <b>id</b>)*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseReferenceMember() throws SyntaxErrorException {
		while (currentToken.type == TokenType.DOT) {
			consume();
			expect(TokenType.IDENTIFIER);
		}
	}

	/**
	 * Parses the <i>Reference</i> non-terminal
	 * 
	 * <pre>
	 * Reference = (this | <b>id</b>) ReferenceMember
	 * ReferenceMember = (. <b>id</b>)*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseReference() throws SyntaxErrorException {
		if (currentToken.type == TokenType.THIS || currentToken.type == TokenType.IDENTIFIER) {
			consume();
			parseReferenceMember();
		} else {
			throw new SyntaxErrorException(currentToken);
		}
	}

	/**
	 * Parses the <i>Statement</i> non-terminal
	 * 
	 * <pre>
	 * Statement ::=
	 *     { Statement* }
	 *     | Type id = Expression <b>;</b>
	 *     | Reference (<b>[</b> Expression <b>]</b>)? = Expression <b>;</b>
	 *     | Reference <b>(</b> ArgumentList? <b>)</b> <b>;</b>
	 *     | <b>if (</b> Expression <b>)</b> Statement (<b>else</b> Statement)?
	 *     | <b>while (</b> Expression <b>)</b> Statement
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseStatement() throws SyntaxErrorException {
		// Starters(Statement) = {, int, boolean, void, this, <id>, if, while
		switch (currentToken.type) {
		case LCURL:
			consume();
			while (currentToken.type != TokenType.RCURL)
				parseStatement();
			expect(TokenType.RCURL);
			break;

		case BOOLEAN:
		case VOID:
			parseType();
			expect(TokenType.IDENTIFIER);
			expect(TokenType.EQUALTO);
			parseExpression();
			expect(TokenType.SEMICOLON);
			break;

		case INT:
			consume();
			// int[] id = Expression;
			if (currentToken.type == TokenType.LSQUARE) {
				consume();
				expect(TokenType.RSQUARE);
			}
			expect(TokenType.IDENTIFIER);
			expect(TokenType.EQUALTO);
			parseExpression();
			expect(TokenType.SEMICOLON);
			break;

		case THIS:
			consume();

			// this.
			parseReferenceMember();

			switch (currentToken.type) {
			// this = Expression;
			case EQUALTO:
				consume();
				parseExpression();
				expect(TokenType.SEMICOLON);
				break;

			// this[Expression] = Expression;
			case LSQUARE:
				consume();
				parseExpression();
				expect(TokenType.RSQUARE);
				expect(TokenType.EQUALTO);
				parseExpression();
				expect(TokenType.SEMICOLON);
				break;

			// this(ArgumentList?);
			case LPAREN:
				consume();
				if (currentToken.type != TokenType.RPAREN)
					parseArgumentList();
				expect(TokenType.RPAREN);
				expect(TokenType.SEMICOLON);
				break;

			default:
				throw new SyntaxErrorException(currentToken);
			}
			break;

		case IDENTIFIER:
			consume();

			switch (currentToken.type) {
			case EQUALTO:
				// id = Expression;
				consume();
				parseExpression();
				expect(TokenType.SEMICOLON);
				break;

			case LSQUARE:
				consume();

				if (currentToken.type == TokenType.RSQUARE) {
					// id[] id = Expression;
					consume();
					expect(TokenType.IDENTIFIER);
				} else {
					// id[Expression] = Expression;
					parseExpression();
					expect(TokenType.RSQUARE);
				}
				expect(TokenType.EQUALTO);
				parseExpression();
				expect(TokenType.SEMICOLON);
				break;

			case DOT:
				parseReferenceMember(); // id(.id)*

				switch (currentToken.type) {

				case EQUALTO:
					// id(.id)* = Expression
					consume();
					parseExpression();
					expect(TokenType.SEMICOLON);
					break;

				case LSQUARE:
					// id(.id)*[Expression] = Expression;
					consume();
					parseExpression();
					expect(TokenType.RSQUARE);
					expect(TokenType.EQUALTO);
					parseExpression();
					expect(TokenType.SEMICOLON);
					break;

				case LPAREN:
					// id(.id)*(ArgumentList?);
					consume();
					if (currentToken.type != TokenType.RPAREN)
						parseArgumentList();
					expect(TokenType.RPAREN);
					expect(TokenType.SEMICOLON);
					break;

				default:
					throw new SyntaxErrorException(currentToken);
				}
				break;

			case IDENTIFIER:
				// id id = Expression;
				consume();
				expect(TokenType.EQUALTO);
				parseExpression();
				expect(TokenType.SEMICOLON);
				break;

			case LPAREN:
				// id (ArgumentList?);
				consume();
				if (currentToken.type != TokenType.RPAREN)
					parseArgumentList();
				expect(TokenType.RPAREN);
				expect(TokenType.SEMICOLON);
				break;

			default:
				throw new SyntaxErrorException(currentToken);
			}
			break;

		case IF:
			consume();
			expect(TokenType.LPAREN);
			parseExpression();
			expect(TokenType.RPAREN);
			parseStatement();
			if (currentToken.type == TokenType.ELSE) {
				consume();
				parseStatement();
			}
			break;

		case WHILE:
			consume();
			expect(TokenType.LPAREN);
			parseExpression();
			expect(TokenType.RPAREN);
			parseStatement();
			break;

		default:
			throw new SyntaxErrorException(currentToken);
		}
	}

	/**
	 * Parses the <i>Expression</i> non-terminal
	 * 
	 * <pre>
	 * Expression ::=  Conjunction ( <b>||</b> Conjunction )*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseExpression() throws SyntaxErrorException {
		parseConjunction();
		while (currentToken.type == TokenType.PIPE_PIPE) {
			consume();
			parseConjunction();
		}
	}

	/**
	 * Parses the <i>Conjunction</i> non-terminal
	 * 
	 * <pre>
	 * Conjunction ::= Equality ( <b>&&</b> Equality )*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseConjunction() throws SyntaxErrorException {
		parseEquality();
		while (currentToken.type == TokenType.AMPERSAND_AMPERSAND) {
			consume();
			parseEquality();
		}
	}

	/**
	 * Parses the <i>Equality</i> non-terminal
	 * 
	 * <pre>
	 * Equality ::= Relational ( ( <b>==</b> | <b>!=</b> ) Relational )*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseEquality() throws SyntaxErrorException {
		parseRelational();
		while (currentToken.type == TokenType.EQUALTO_EQUALTO || currentToken.type == TokenType.BANG_EQUALTO) {
			consume();
			parseRelational();
		}
	}

	/**
	 * Parses the <i>Relational</i> non-terminal
	 * 
	 * <pre>
	 * Relational ::= Additive ( ( <b>&lt;=</b> | <b>&lt;</b> | <b>&gt;</b> | <b>&gt;=</b> ) Additive )*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseRelational() throws SyntaxErrorException {
		parseAdditive();
		while (currentToken.type == TokenType.LANGLE_EQUALTO || currentToken.type == TokenType.LANGLE
				|| currentToken.type == TokenType.RANGLE || currentToken.type == TokenType.RANGLE_EQUALTO) {
			consume();
			parseAdditive();
		}
	}

	/**
	 * Parses the <i>Additive</i> non-terminal
	 * 
	 * <pre>
	 * Additive ::= Multiplicative ( ( <b>+</b> | <b>&minus;</b> ) Multiplicative )*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseAdditive() throws SyntaxErrorException {
		parseMultiplicative();
		while (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS) {
			consume();
			parseMultiplicative();
		}
	}

	/**
	 * Parses the <i>Multiplicative</i> non-terminal
	 * 
	 * <pre>
	 * Multiplicative ::= Term ( ( <b>*</b> | <b>/</b> ) Term )*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseMultiplicative() throws SyntaxErrorException {
		parseTerm();
		while (currentToken.type == TokenType.ASTERISK || currentToken.type == TokenType.SLASH) {
			consume();
			parseTerm();
		}
	}

	/**
	 * Parses the <i>Term</i> non-terminal
	 * 
	 * <pre>
	 * Term ::= <b>(</b> Expression <b>)</b> 
	 *       | ( <b>&minus;</b> | <b>!</b> ) Expression 
	 *       | <b>num</b> | <b>true</b> | <b>false</b>
	 *       | Reference ( <b>[</b> Expression <b>]</b> )?
	 *       | Reference <b>(</b> ArgumentList? <b>)</b>
	 *       | <b>new</b> (id <b>( )</b> | <b>int [</b> Expression <b>]</b> | id <b>[</b> Expression <b>]</b> )
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private void parseTerm() throws SyntaxErrorException {
		switch (currentToken.type) {
		case NEW:
			consume();

			switch (currentToken.type) {
			case INT:
				// new int [ Expression ]
				consume();
				expect(TokenType.LSQUARE);
				parseExpression();
				expect(TokenType.RSQUARE);
				break;

			case IDENTIFIER:
				consume();
				switch (currentToken.type) {
				case LPAREN:
					// new id ( )
					consume();
					expect(TokenType.RPAREN);
					break;

				case LSQUARE:
					// new id [ Expression ]
					consume();
					parseExpression();
					expect(TokenType.RSQUARE);
					break;

				default:
					throw new SyntaxErrorException(currentToken);
				}
			}
			break;

		case THIS:
		case IDENTIFIER:
			parseReference();

			switch (currentToken.type) {
			case LSQUARE:
				// Reference[Expression]
				consume();
				parseExpression();
				expect(TokenType.RSQUARE);
				break;

			case LPAREN:
				// Reference(Expression)
				consume();
				if (currentToken.type != TokenType.RPAREN)
					parseArgumentList();
				expect(TokenType.RPAREN);
				break;

			default:
				// Reference
				break;
			}
			break;

		case NUMBER:
		case TRUE:
		case FALSE:
			consume();
			return;

		case MINUS:
		case BANG:
			// ( - | ! ) Expression
			consume();
			parseExpression();
			return;

		case LPAREN:
			// ( Expression )
			consume();
			parseExpression();
			expect(TokenType.RPAREN);
			return;

		default:
			throw new SyntaxErrorException(currentToken);
		}
	}
}
