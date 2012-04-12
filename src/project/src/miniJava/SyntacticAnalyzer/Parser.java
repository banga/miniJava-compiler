package miniJava.SyntacticAnalyzer;

import java.io.InputStream;
import java.util.Stack;

import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassDeclList;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.ExprList;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IdentifierList;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IndexedRef;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.Literal;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QualifiedRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Reference;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.Type;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.WhileStmt;

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
	public miniJava.AbstractSyntaxTrees.Package parseProgram() throws SyntaxErrorException {
		consume();
		SourcePosition packagePos = currentToken.position;
		ClassDeclList packageClassList = new ClassDeclList();
		while (currentToken.type != TokenType.EOT) {
			packageClassList.add(parseClassDeclaration());
		}
		return new miniJava.AbstractSyntaxTrees.Package(packageClassList, packagePos);
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
	private ClassDecl parseClassDeclaration() throws SyntaxErrorException {
		SourcePosition classPos = currentToken.position;
		expect(TokenType.CLASS);
		Identifier classId = new Identifier(currentToken.spelling, currentToken.position);
		expect(TokenType.IDENTIFIER);
		expect(TokenType.LCURL);
		FieldDeclList fieldList = new FieldDeclList();
		MethodDeclList methodList = new MethodDeclList();

		while (currentToken.type != TokenType.RCURL) {
			// Left factorize the Declarators and id
			MemberDecl memberDecl = parseDeclarators();
			expect(TokenType.IDENTIFIER);

			if (currentToken.type == TokenType.LPAREN) {
				// MethodDeclaration
				consume();
				ParameterDeclList pList = new ParameterDeclList();
				if (currentToken.type != TokenType.RPAREN)
					pList = parseParameterList();
				expect(TokenType.RPAREN);

				// Method body
				expect(TokenType.LCURL);
				StatementList methodStmtList = new StatementList();
				Expression returnExpr = null;
				while (currentToken.type != TokenType.RCURL) {
					if (currentToken.type == TokenType.RETURN) {
						consume();
						returnExpr = parseExpression();
						expect(TokenType.SEMICOLON);
						break;
					} else {
						methodStmtList.add(parseStatement());
					}
				}
				expect(TokenType.RCURL);
				MethodDecl methodDecl = new MethodDecl(memberDecl, pList, methodStmtList, returnExpr, memberDecl.posn);
				methodList.add(methodDecl);
			} else {
				FieldDecl fieldDecl = new FieldDecl(memberDecl, memberDecl.posn);
				fieldList.add(fieldDecl);
				expect(TokenType.SEMICOLON);
			}
		}
		expect(TokenType.RCURL);
		return new ClassDecl(classId, fieldList, methodList, classPos);
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
	private FieldDecl parseDeclarators() throws SyntaxErrorException {
		boolean isPrivate = false;
		boolean isStatic = false;
		SourcePosition currPos = currentToken.position;
		if (currentToken.type == TokenType.PUBLIC || currentToken.type == TokenType.PRIVATE) {
			isPrivate = (currentToken.type == TokenType.PRIVATE);
			consume();
		}
		if (currentToken.type == TokenType.STATIC) {
			isStatic = true;
			consume();
		}
		Type memberType = parseType();
		Identifier memberId = new Identifier(currentToken.spelling, currentToken.position);
		return new FieldDecl(isPrivate, isStatic, memberType, memberId, currPos);
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
	private Type parseType() throws SyntaxErrorException {
		Type type;
		SourcePosition typePos = currentToken.position;

		switch (currentToken.type) {
		case INT:
		case IDENTIFIER:
			switch (currentToken.type) {
			case INT:
				type = new BaseType(TypeKind.INT, currentToken.spelling, typePos);
				break;

			case IDENTIFIER:
				Identifier typeId = new Identifier(currentToken.spelling, typePos);
				type = new ClassType(typeId.spelling, typeId.posn);
				break;

			default:
				throw new SyntaxErrorException(currentToken);
			}
			consume();
			if (currentToken.type == TokenType.LSQUARE) {
				// ArrayType
				consume();
				expect(TokenType.RSQUARE);
				type = new ArrayType(type, currentToken.spelling, typePos);
			}
			break;

		case BOOLEAN:
		case VOID:
			switch (currentToken.type) {
			case BOOLEAN:
				type = new BaseType(TypeKind.BOOLEAN, currentToken.spelling, typePos);
				break;
			case VOID:
				type = new BaseType(TypeKind.VOID, currentToken.spelling, typePos);
				break;
			default:
				throw new SyntaxErrorException(currentToken);
			}
			consume();
			break;

		default:
			throw new SyntaxErrorException("expected a Type, found ", currentToken);
		}

		return type;
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
	private ParameterDeclList parseParameterList() throws SyntaxErrorException {
		ParameterDeclList pList = new ParameterDeclList();
		Type type = parseType();
		Identifier id = new Identifier(currentToken.spelling, currentToken.position);
		pList.add(new ParameterDecl(type, id, type.posn));

		expect(TokenType.IDENTIFIER);
		while (currentToken.type == TokenType.COMMA) {
			consume();
			type = parseType();
			id = new Identifier(currentToken.spelling, currentToken.position);
			pList.add(new ParameterDecl(type, id, type.posn));
			expect(TokenType.IDENTIFIER);
		}
		return pList;
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
	private ExprList parseArgumentList() throws SyntaxErrorException {
		ExprList exprs = new ExprList();
		Expression expr;
		expr = parseExpression();
		exprs.add(expr);
		while (currentToken.type == TokenType.COMMA) {
			consume();
			expr = parseExpression();
			exprs.add(expr);
		}
		return exprs;
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
	private IdentifierList parseReferenceMember(Identifier id) throws SyntaxErrorException {
		IdentifierList idList = new IdentifierList();
		if (id != null) // if reference starts with an id instead of this
			idList.add(id);
		while (currentToken.type == TokenType.DOT) {
			consume();
			idList.add(new Identifier(currentToken.spelling, currentToken.position));
			expect(TokenType.IDENTIFIER);
		}
		return idList;
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
	private Reference parseReference() throws SyntaxErrorException {
		Reference reference = null;
		SourcePosition referencePos = currentToken.position;

		if (currentToken.type == TokenType.THIS || currentToken.type == TokenType.IDENTIFIER) {
			boolean isThis = (currentToken.type == TokenType.THIS);
			Identifier id = isThis ? null : new Identifier(currentToken.spelling, currentToken.position);
			consume();
			IdentifierList idList = parseReferenceMember(id);
			reference = new QualifiedRef(isThis, idList, referencePos);
		} else {
			throw new SyntaxErrorException(currentToken);
		}
		return reference;
	}

	/**
	 * Parses the <i>Statement</i> non-terminal
	 * 
	 * <pre>
	 * Statement ::=
	 *     { Statement* }                                  // BlockStmt
	 *     | Type id = Expression <b>;</b>                        // VarDeclStmt
	 *     | Reference (<b>[</b> Expression <b>]</b>)? = Expression <b>;</b>    // AssignStmt
	 *     | Reference <b>(</b> ArgumentList? <b>)</b> <b>;</b>                 // CallStmt
	 *     | <b>if (</b> Expression <b>)</b> Statement (<b>else</b> Statement)? // IfStmt
	 *     | <b>while (</b> Expression <b>)</b> Statement                // WhileStmt
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private Statement parseStatement() throws SyntaxErrorException {
		// Starters(Statement) = {, int, boolean, void, this, <id>, if, while
		Statement stmt;
		SourcePosition stmtPos = currentToken.position;
		switch (currentToken.type) {
		case LCURL:
			consume();
			StatementList stmtList = new StatementList();
			while (currentToken.type != TokenType.RCURL) {
				stmtList.add(parseStatement());
			}
			expect(TokenType.RCURL);
			stmt = new BlockStmt(stmtList, stmtPos);
			break;

		case BOOLEAN:
		case VOID:
			Type varDeclType = parseType();
			Identifier varDeclId = new Identifier(currentToken.spelling, currentToken.position);
			expect(TokenType.IDENTIFIER);
			expect(TokenType.EQUALTO);
			Expression varDeclExpr = parseExpression();
			expect(TokenType.SEMICOLON);
			VarDecl varDecl1 = new VarDecl(varDeclType, varDeclId, stmtPos);
			stmt = new VarDeclStmt(varDecl1, varDeclExpr, stmtPos);
			break;

		case INT:
			Type intVarDeclType = new BaseType(TypeKind.INT, currentToken.spelling, currentToken.position);
			consume();
			// int[] id = Expression;
			if (currentToken.type == TokenType.LSQUARE) {
				consume();
				intVarDeclType = new ArrayType(intVarDeclType, intVarDeclType.spelling, intVarDeclType.posn);
				expect(TokenType.RSQUARE);
			}
			Identifier intVarDeclId = new Identifier(currentToken.spelling, currentToken.position);
			expect(TokenType.IDENTIFIER);
			expect(TokenType.EQUALTO);
			Expression intVarDeclExpr = parseExpression();
			expect(TokenType.SEMICOLON);
			VarDecl varDecl2 = new VarDecl(intVarDeclType, intVarDeclId, stmtPos);
			stmt = new VarDeclStmt(varDecl2, intVarDeclExpr, stmtPos);
			break;

		case THIS:
			consume();

			// this.
			IdentifierList thisRefList = parseReferenceMember(null);
			Reference thisRef = new QualifiedRef(true, thisRefList, stmtPos);
			switch (currentToken.type) {
			// this = Expression;
			case EQUALTO:
				consume();
				Expression thisRefExpr1 = parseExpression();
				expect(TokenType.SEMICOLON);
				stmt = new AssignStmt(thisRef, thisRefExpr1, stmtPos);
				break;

			// this[Expression] = Expression;
			case LSQUARE:
				consume();
				Expression thisRefExpr2 = parseExpression();
				thisRef = new IndexedRef(thisRef, thisRefExpr2, stmtPos);
				expect(TokenType.RSQUARE);
				expect(TokenType.EQUALTO);
				Expression thisRefExpr3 = parseExpression();
				expect(TokenType.SEMICOLON);
				stmt = new AssignStmt(thisRef, thisRefExpr3, stmtPos);
				break;

			// this(ArgumentList?);
			case LPAREN:
				consume();
				ExprList thisRefExprList = new ExprList();
				if (currentToken.type != TokenType.RPAREN) {
					thisRefExprList = parseArgumentList();
				}
				expect(TokenType.RPAREN);
				expect(TokenType.SEMICOLON);
				stmt = new CallStmt(thisRef, thisRefExprList, stmtPos);
				break;

			default:
				throw new SyntaxErrorException(currentToken);
			}
			break;

		case IDENTIFIER:
			Identifier id1 = new Identifier(currentToken.spelling, currentToken.position);
			Reference idRef1 = new QualifiedRef(id1);
			consume();

			switch (currentToken.type) {
			case EQUALTO:
				// id = Expression; //AssignStmt
				consume();
				Expression idExpr1 = parseExpression();
				expect(TokenType.SEMICOLON);
				stmt = new AssignStmt(idRef1, idExpr1, stmtPos);
				break;

			case LSQUARE:
				consume();

				if (currentToken.type == TokenType.RSQUARE) {
					// id[] id = Expression; //VarDeclStmt
					Type idType = new ClassType(id1.spelling, id1.posn);
					Type idArrType = new ArrayType(idType, idType.spelling, idType.posn);
					consume();
					Identifier id2 = new Identifier(currentToken.spelling, currentToken.position);
					expect(TokenType.IDENTIFIER);
					VarDecl idVarDecl = new VarDecl(idArrType, id2, stmtPos);
					expect(TokenType.EQUALTO);
					Expression idExpr2 = parseExpression();
					expect(TokenType.SEMICOLON);
					stmt = new VarDeclStmt(idVarDecl, idExpr2, stmtPos);
				} else {
					// id[Expression] = Expression; //AssignStmt
					Expression idExpr3 = parseExpression();
					idRef1 = new IndexedRef(idRef1, idExpr3, stmtPos);
					expect(TokenType.RSQUARE);
					expect(TokenType.EQUALTO);
					Expression idExpr4 = parseExpression();
					expect(TokenType.SEMICOLON);
					stmt = new AssignStmt(idRef1, idExpr4, stmtPos);
				}
				break;

			case DOT:
				Expression idRefExpr1;
				IdentifierList idList1 = parseReferenceMember(id1); // id(.id)*
				idRef1 = new QualifiedRef(false, idList1, stmtPos);

				switch (currentToken.type) {

				case EQUALTO:
					// id(.id)* = Expression //AssignStmt
					consume();
					idRefExpr1 = parseExpression();
					expect(TokenType.SEMICOLON);
					stmt = new AssignStmt(idRef1, idRefExpr1, stmtPos);
					break;

				case LSQUARE:
					// id(.id)*[Expression] = Expression; //AssignStmt
					consume();
					idRefExpr1 = parseExpression();
					idRef1 = new IndexedRef(idRef1, idRefExpr1, stmtPos);
					expect(TokenType.RSQUARE);
					expect(TokenType.EQUALTO);
					Expression idRefExpr2 = parseExpression();
					expect(TokenType.SEMICOLON);
					stmt = new AssignStmt(idRef1, idRefExpr2, stmtPos);
					break;

				case LPAREN:
					// id(.id)*(ArgumentList?); //CallStmt
					consume();
					ExprList idRefExprList = new ExprList();
					if (currentToken.type != TokenType.RPAREN) {
						idRefExprList = parseArgumentList();
					}
					expect(TokenType.RPAREN);
					expect(TokenType.SEMICOLON);
					stmt = new CallStmt(idRef1, idRefExprList, stmtPos);
					break;

				default:
					throw new SyntaxErrorException(currentToken);
				}
				break;

			case IDENTIFIER:
				// id id = Expression; //VarDeclStmt
				Identifier id2 = new Identifier(currentToken.spelling, currentToken.position);
				Type id1Class = new ClassType(id1.spelling, id1.posn);
				consume();
				expect(TokenType.EQUALTO);
				Expression idIdExpr1 = parseExpression();
				expect(TokenType.SEMICOLON);
				VarDecl idIdVarDecl = new VarDecl(id1Class, id2, stmtPos);
				stmt = new VarDeclStmt(idIdVarDecl, idIdExpr1, stmtPos);
				break;

			case LPAREN:
				// id (ArgumentList?); //Callstmt
				consume();
				ExprList idIdExprList = new ExprList();
				if (currentToken.type != TokenType.RPAREN) {
					idIdExprList = parseArgumentList();
				}
				expect(TokenType.RPAREN);
				expect(TokenType.SEMICOLON);
				stmt = new CallStmt(idRef1, idIdExprList, stmtPos);
				break;

			default:
				throw new SyntaxErrorException(currentToken);
			}
			break;

		case IF:
			consume();
			expect(TokenType.LPAREN);
			Expression ifExpr = parseExpression();
			expect(TokenType.RPAREN);
			Statement ifBlock = parseStatement();
			if (currentToken.type == TokenType.ELSE) {
				consume();
				stmt = new IfStmt(ifExpr, ifBlock, parseStatement(), stmtPos);
			} else {
				stmt = new IfStmt(ifExpr, ifBlock, stmtPos);
			}
			break;

		case WHILE:
			consume();
			expect(TokenType.LPAREN);
			Expression whileExpr = parseExpression();
			expect(TokenType.RPAREN);
			stmt = new WhileStmt(whileExpr, parseStatement(), stmtPos);
			break;

		default:
			throw new SyntaxErrorException(currentToken);
		}
		return stmt;
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
	private Expression parseExpression() throws SyntaxErrorException {
		Expression expr = parseConjunction();
		while (currentToken.type == TokenType.PIPE_PIPE) {
			Operator op = new Operator(currentToken.spelling, currentToken.position);
			consume();
			expr = new BinaryExpr(op, expr, parseConjunction(), currentToken.position);
		}
		return expr;
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
	private Expression parseConjunction() throws SyntaxErrorException {
		Expression expr = parseEquality();
		while (currentToken.type == TokenType.AMPERSAND_AMPERSAND) {
			Operator op = new Operator(currentToken.spelling, currentToken.position);
			consume();
			expr = new BinaryExpr(op, expr, parseEquality(), currentToken.position);
		}
		return expr;
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
	private Expression parseEquality() throws SyntaxErrorException {
		Expression expr = parseRelational();
		while (currentToken.type == TokenType.EQUALTO_EQUALTO || currentToken.type == TokenType.BANG_EQUALTO) {
			Operator op = new Operator(currentToken.spelling, currentToken.position);
			consume();
			expr = new BinaryExpr(op, expr, parseRelational(), currentToken.position);
		}
		return expr;
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
	private Expression parseRelational() throws SyntaxErrorException {
		Expression expr = parseAdditive();
		while (currentToken.type == TokenType.LANGLE_EQUALTO || currentToken.type == TokenType.LANGLE
				|| currentToken.type == TokenType.RANGLE || currentToken.type == TokenType.RANGLE_EQUALTO) {
			Operator op = new Operator(currentToken.spelling, currentToken.position);
			consume();
			expr = new BinaryExpr(op, expr, parseAdditive(), currentToken.position);
		}
		return expr;
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
	private Expression parseAdditive() throws SyntaxErrorException {
		Expression expr = parseMultiplicative();
		while (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS) {
			Operator op = new Operator(currentToken.spelling, currentToken.position);
			consume();
			expr = new BinaryExpr(op, expr, parseMultiplicative(), currentToken.position);
		}
		return expr;
	}

	/**
	 * Parses the <i>Multiplicative</i> non-terminal
	 * 
	 * <pre>
	 * Multiplicative ::= Unary ( ( <b>*</b> | <b>/</b> ) Unary )*
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private Expression parseMultiplicative() throws SyntaxErrorException {
		Expression expr = parseUnary();
		while (currentToken.type == TokenType.ASTERISK || currentToken.type == TokenType.SLASH) {
			Operator op = new Operator(currentToken.spelling, currentToken.position);
			consume();
			expr = new BinaryExpr(op, expr, parseUnary(), currentToken.position);
		}
		return expr;
	}

	/**
	 * Parses the <i>Unary</i> non-terminal
	 * 
	 * <pre>
	 * Unary ::= ( <b>&minus;</b> | <b>!</b> )* Term
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private Expression parseUnary() throws SyntaxErrorException {
		Stack<Operator> operators = new Stack<Operator>();
		while (currentToken.type == TokenType.MINUS || currentToken.type == TokenType.BANG) {
			operators.push(new Operator(currentToken.spelling, currentToken.position));
			consume();
		}
		Expression expr = parseTerm();
		while (!operators.empty()) {
			Operator op = operators.pop();
			expr = new UnaryExpr(op, expr, op.posn);
		}
		return expr;
	}

	/**
	 * Parses the <i>Term</i> non-terminal
	 * 
	 * <pre>
	 * Term ::= <b>(</b> Expression <b>)</b>                 // Expression
	 *       | <b>num</b> | <b>true</b> | <b>false</b>              // LiteralExpr
	 *       | Reference ( <b>[</b> Expression <b>]</b> )?   // RefExpr
	 *       | Reference <b>(</b> ArgumentList? <b>)</b>     // CallExpr
	 *       | <b>new</b> (id <b>( )</b> | <b>int [</b> Expression <b>]</b> | id <b>[</b> Expression <b>]</b> )  // NewObjectExpr, NewArrayExpr
	 * </pre>
	 * 
	 * @throws SyntaxErrorException
	 */
	private Expression parseTerm() throws SyntaxErrorException {
		Expression expr = null;
		SourcePosition exprPos = currentToken.position;

		switch (currentToken.type) {
		case NEW:
			consume();
			Type newType;
			Expression arrayExpr;

			switch (currentToken.type) {
			case INT:
				// new int [ Expression ]
				newType = new BaseType(TypeKind.INT, currentToken.spelling, currentToken.position);
				consume();
				expect(TokenType.LSQUARE);
				arrayExpr = parseExpression();
				expect(TokenType.RSQUARE);
				expr = new NewArrayExpr(newType, arrayExpr, exprPos);
				break;

			case IDENTIFIER:
				newType = new ClassType(currentToken.spelling, currentToken.position);
				consume();
				switch (currentToken.type) {
				case LPAREN:
					// new id ( )
					consume();
					expect(TokenType.RPAREN);
					expr = new NewObjectExpr((ClassType) newType, exprPos);
					break;

				case LSQUARE:
					// new id [ Expression ]
					consume();
					arrayExpr = parseExpression();
					expect(TokenType.RSQUARE);
					expr = new NewArrayExpr(newType, arrayExpr, exprPos);
					break;

				default:
					throw new SyntaxErrorException(currentToken);
				}
			}
			break;

		case THIS:
		case IDENTIFIER:
			Reference idRef = parseReference();
			Expression idExpr;

			switch (currentToken.type) {
			case LSQUARE:
				// Reference[Expression]
				consume();
				idExpr = parseExpression();
				expect(TokenType.RSQUARE);
				IndexedRef indexedRef1 = new IndexedRef(idRef, idExpr, idRef.posn);
				expr = new RefExpr(indexedRef1, exprPos);
				break;

			case LPAREN:
				// Reference(Expression)
				consume();
				ExprList argList = new ExprList();
				if (currentToken.type != TokenType.RPAREN)
					argList = parseArgumentList();
				expect(TokenType.RPAREN);
				expr = new CallExpr(idRef, argList, exprPos);
				break;

			default:
				// Reference
				expr = new RefExpr(idRef, exprPos);
				break;
			}
			break;

		case NUMBER:
		case TRUE:
		case FALSE:
			Literal literal;
			if (currentToken.type == TokenType.NUMBER) {
				literal = new IntLiteral(currentToken.spelling, currentToken.position);
			} else {
				literal = new BooleanLiteral(currentToken.spelling, currentToken.position);
			}
			consume();
			expr = new LiteralExpr(literal, exprPos);
			break;

		case LPAREN:
			// ( Expression )
			consume();
			expr = parseExpression();
			expect(TokenType.RPAREN);
			break;

		default:
			throw new SyntaxErrorException(currentToken);
		}

		return expr;
	}
}
