package miniJava.ContextualAnalyzer;

import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
import miniJava.AbstractSyntaxTrees.BadRef;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassRef;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.DeRef;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.ErrorType;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.ForStmt;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IndexedRef;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.LocalRef;
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MemberRef;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.NullLiteral;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.OverloadedMethodDecl;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.QualifiedRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementType;
import miniJava.AbstractSyntaxTrees.StringLiteral;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;

public class ASTIdentifyMembers implements Visitor<IdentificationTable, Void> {
	/**
	 * Creates an identification table for the program
	 * 
	 * @param ast
	 * @return IdentificationTable object
	 */
	public IdentificationTable createIdentificationTable(AST ast) {
		IdentificationTable table = new IdentificationTable();
		ast.visit(this, table);
		return table;
	}

	/**
	 * When visiting a package, we just add the class declarations to the table,
	 * check for the main method and exit. The member declarations are *NOT*
	 * visited.
	 */
	@Override
	public Void visitPackage(Package prog, IdentificationTable table) {
		for (ClassDecl cd : prog.classDeclList) {
			Utilities.addDeclaration(table, cd);
		}

		// Associate super class declarations
		for (ClassDecl cd : prog.classDeclList) {
			if (cd.superClass != null)
				cd.superClass.visit(this, table);
		}

		return null;
	}

	/**
	 * This is called from the ASTReplaceReference visitor, which adds member
	 * declarations to the identification table of the given class
	 */
	@Override
	public Void visitClassDecl(ClassDecl cd, IdentificationTable table) {
		cd.id.declaration = cd;
		currentClass = cd;

		table.openScope();

		for (OverloadedMethodDecl omd : cd.methodDeclList)
			Utilities.addDeclaration(table, omd);

		for (FieldDecl fd : cd.fieldDeclList)
			fd.visit(this, table);

		// Types must be visited only after declarations have been added
		for (FieldDecl fd : cd.fieldDeclList)
			fd.type.visit(this, table);

		// Finally visit the methods
		for (OverloadedMethodDecl methodDecl : cd.methodDeclList)
			methodDecl.visit(this, table);

		// Also visit the constructors
		cd.constructorDecl.visit(this, table);

		return null;
	}

	@Override
	public Void visitFieldDecl(FieldDecl fd, IdentificationTable table) {
		fd.id.declaration = fd;

		if (fd.isStatic) {
			Utilities.reportError("Static fields are not allowed", fd.posn);
		} else {
			if (fd.type.typeKind == TypeKind.VOID) {
				Utilities.reportError("void is an invalid type for the field " + fd.id.spelling, fd.posn);
			} else {
				Utilities.addDeclaration(table, fd);
			}
		}

		if (fd.initExpr != null)
			fd.initExpr.visit(this, table);

		return null;
	}

	@Override
	public Void visitOverloadedMethodDecl(OverloadedMethodDecl omd, IdentificationTable table) {
		for (MethodDecl md : omd)
			md.visit(this, table);

		return null;
	}

	@Override
	public Void visitMethodDecl(MethodDecl md, IdentificationTable table) {
		md.id.declaration = md;
		currentMethod = md;

		md.type.visit(this, table);

		// Parameter scope
		table.openScope();

		for (ParameterDecl pd : md.parameterDeclList) {
			pd.visit(this, table);
		}

		// Types must be visited only after all the declarations have been added
		for (ParameterDecl pd : md.parameterDeclList) {
			pd.type.visit(this, table);
		}

		// Scope of local variables
		table.openScope();

		for (Statement st : md.statementList)
			st.visit(this, table);

		if (md.returnExp != null)
			md.returnExp.visit(this, table);

		table.closeScope();

		table.closeScope();

		currentMethod = null;

		return null;
	}

	@Override
	public Void visitParameterDecl(ParameterDecl pd, IdentificationTable table) {
		pd.id.declaration = pd;

		if (pd.type.typeKind == TypeKind.VOID)
			Utilities.reportError("void is an invalid type for the variable " + pd.id.spelling, pd.posn);

		Utilities.addDeclaration(table, pd);

		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd, IdentificationTable table) {
		vd.id.declaration = vd;

		return null;
	}

	@Override
	public Void visitBaseType(BaseType type, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitClassType(ClassType type, IdentificationTable table) {
		Declaration decl = table.get(type.spelling);

		if (!(decl instanceof ClassDecl)) {
			Utilities.reportError(type.spelling + " cannot be resolved to a type", type.posn);
			return null;
		}

		type.declaration = (ClassDecl) decl;

		return null;
	}

	@Override
	public Void visitArrayType(ArrayType type, IdentificationTable table) {
		type.eltType.visit(this, table);

		return null;
	}

	@Override
	public Void visitStatementType(StatementType type, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitErrorType(ErrorType type, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitBlockStmt(BlockStmt stmt, IdentificationTable table) {
		// nested scope
		table.openScope();

		for (Statement s : stmt.sl)
			s.visit(this, table);

		table.closeScope();

		return null;
	}

	@Override
	public Void visitVardeclStmt(VarDeclStmt stmt, IdentificationTable table) {
		if (stmt.varDecl.type.typeKind == TypeKind.VOID)
			Utilities.reportError("void is an invalid type for the variable " + stmt.varDecl.id.spelling,
					stmt.varDecl.posn);

		stmt.varDecl.type.visit(this, table);
		stmt.varDecl.visit(this, table);

		// Add the declaration of this identifier at this point
		// Catches the A A = d; case

		Utilities.addDeclaration(table, stmt.varDecl);
		stmt.initExp.visit(this, table);
		stmt.varDecl.initialized = true;

		return null;
	}

	@Override
	public Void visitAssignStmt(AssignStmt stmt, IdentificationTable table) {
		stmt.ref.visit(this, table);
		stmt.val.visit(this, table);

		return null;
	}

	@Override
	public Void visitCallStmt(CallStmt stmt, IdentificationTable table) {
		stmt.methodRef.visit(this, table);
		for (Expression e : stmt.argList)
			e.visit(this, table);

		return null;
	}

	@Override
	public Void visitIfStmt(IfStmt stmt, IdentificationTable table) {
		stmt.cond.visit(this, table);
		if (stmt.thenStmt instanceof VarDeclStmt) {
			Utilities.reportError("Variable declaration cannot be the only statement in a conditional statement",
					stmt.thenStmt.posn);
		} else {
			stmt.thenStmt.visit(this, table);
		}

		if (stmt.elseStmt != null) {
			if (stmt.elseStmt instanceof VarDeclStmt) {
				Utilities.reportError("Variable declaration cannot be the only statement in a conditional statement",
						stmt.elseStmt.posn);
			} else {
				stmt.elseStmt.visit(this, table);
			}
		}

		return null;
	}

	@Override
	public Void visitWhileStmt(WhileStmt stmt, IdentificationTable table) {
		stmt.cond.visit(this, table);

		if (stmt.body instanceof VarDeclStmt) {
			Utilities.reportError("Variable declaration cannot be the only statement in a while statement",
					stmt.body.posn);
		} else {
			stmt.body.visit(this, table);
		}

		return null;
	}

	@Override
	public Void visitForStmt(ForStmt stmt, IdentificationTable table) {
		table.openScope();
		
		stmt.init.visit(this, table);
		stmt.cond.visit(this, table);
		stmt.incr.visit(this, table);

		if (stmt.body instanceof VarDeclStmt) {
			Utilities.reportError("Variable declaration cannot be the only statement in a for statement",
					stmt.body.posn);
		} else if (stmt.incr instanceof VarDeclStmt) {
			Utilities.reportError("Variable declaration cannot be an increment statement in the for loop",
					stmt.body.posn);
		}  
		stmt.body.visit(this, table);
		

		table.closeScope();
		return null;
	}

	@Override
	public Void visitUnaryExpr(UnaryExpr expr, IdentificationTable table) {
		expr.operator.visit(this, table);
		expr.expr.visit(this, table);

		return null;
	}

	@Override
	public Void visitBinaryExpr(BinaryExpr expr, IdentificationTable table) {
		expr.operator.visit(this, table);
		expr.left.visit(this, table);
		expr.right.visit(this, table);

		return null;
	}

	@Override
	public Void visitRefExpr(RefExpr expr, IdentificationTable table) {
		expr.ref.visit(this, table);

		return null;
	}

	@Override
	public Void visitCallExpr(CallExpr expr, IdentificationTable table) {
		expr.functionRef.visit(this, table);
		for (Expression e : expr.argList) {
			e.visit(this, table);
		}

		return null;
	}

	@Override
	public Void visitLiteralExpr(LiteralExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitNewObjectExpr(NewObjectExpr expr, IdentificationTable table) {
		expr.classtype.visit(this, table);

		for (Expression e : expr.argList)
			e.visit(this, table);

		return null;
	}

	@Override
	public Void visitNewArrayExpr(NewArrayExpr expr, IdentificationTable table) {
		expr.eltType.visit(this, table);
		expr.sizeExpr.visit(this, table);

		return null;
	}

	@Override
	public Void visitQualifiedRef(QualifiedRef qRef, IdentificationTable table) {
		Identifier id;
		boolean isThisRef = false;

		if (qRef.thisRelative) {
			if (currentMethod.isStatic) {
				Utilities.reportError("Cannot use this in static context", qRef.posn);
				return null;
			}

			if (qRef.qualifierList.size() == 0) {
				// Just the 'this' identifier, returning here because no more
				// identification is required and it breaks the last check
				// otherwise
				return null;
			}

			isThisRef = true;
			id = new Identifier("this", qRef.posn);
			id.declaration = currentClass;
		} else {
			id = qRef.qualifierList.get(0);

			int scope = table.linkDeclaration(id);

			if (scope == IdentificationTable.INVALID_SCOPE) {
				Utilities.reportError("Undeclared identifier '" + id + "'", id.posn);
				return null;
			}

			if (currentMethod != null) {
				// Non-static members from static method
				if (scope == IdentificationTable.MEMBER_SCOPE && currentMethod.isStatic
						&& !((MemberDecl) id.declaration).isStatic) {
					Utilities.reportError("Non-static member " + id + " cannot be accessed from static method "
							+ currentMethod.id, id.posn);
					return null;
				}
			}

			// Handle int x = x + 2; case
			if (id.declaration instanceof VarDecl && !((VarDecl) id.declaration).initialized) {
				Utilities.reportError("Local variable " + id + " may not have been initialized", id.posn);
			}
		}

		// Handle subsequent Identifiers in the QualifiedRef list
		for (int i = (isThisRef) ? 0 : 1; i < qRef.qualifierList.size(); i++) {
			boolean currentIdentifierIsThis = (i == 0);
			// Identifiers indexed > 0 in the QualifiedRef list are always
			// Members of Class. Get the parent to which the
			// current Identifier is a member of.
			Identifier parentID = id;
			// boolean isParentClassName = parentID.declaration instanceof
			// ClassDecl;

			ClassDecl parentClassDecl;

			if (parentID.declaration.type instanceof ClassType) {
				ClassType parentClassType = (ClassType) parentID.declaration.type;
				parentClassType.visit(this, table);
				if (!(parentClassType.declaration instanceof ClassDecl)) {
					Utilities.reportError(parentClassType.spelling + " is not a valid type", parentID.posn);
					return null;
				}
				parentClassDecl = (ClassDecl) parentClassType.declaration;
			} else if (parentID.declaration.type instanceof ArrayType) {
				ArrayType parentArrayType = (ArrayType) parentID.declaration.type;
				parentArrayType.visit(this, table);
				parentClassDecl = parentArrayType.declaration;
			} else {
				Utilities.reportError(parentID + " is not an instance or a class", parentID.posn);
				return null;
			}

			// Can we access private members?
			boolean hasPrivateAccess = (parentClassDecl == currentClass);
			// Are we accessing static members?
			boolean isStaticReference = (parentID.declaration instanceof ClassDecl) && !currentIdentifierIsThis;

			id = qRef.qualifierList.get(i);
			id.declaration = parentClassDecl.getFieldDeclaration(id, hasPrivateAccess, isStaticReference);

			if (id.declaration == null) {
				if (i == qRef.qualifierList.size() - 1) {
					// Could be a method if this is the last qualifier
					id.declaration = parentClassDecl.getMethodDeclaration(id, hasPrivateAccess, isStaticReference);
					if (id.declaration == null) {
						Utilities.reportError(id + " is not a member of " + parentClassDecl.id, id.posn);
						return null;
					}
				} else {
					Utilities.reportError(id + " is not a field of " + parentClassDecl.id, id.posn);
					return null;
				}
			}
		}

		if (id.declaration instanceof ClassDecl) {
			Utilities.reportError(id + " cannot be resolved to a variable", id.posn);
		}

		return null;
	}

	@Override
	public Void visitIndexedRef(IndexedRef ref, IdentificationTable table) {
		ref.ref.visit(this, table);
		ref.indexExpr.visit(this, table);

		return null;
	}

	@Override
	public Void visitIdentifier(Identifier id, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitOperator(Operator op, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral num, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitNullLiteral(NullLiteral num, IdentificationTable arg) { 
		return null;
	}
	@Override
	public Void visitBooleanLiteral(BooleanLiteral bool, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitStringLiteral(StringLiteral bool, IdentificationTable arg) {
		return null;
	}

	@Override
	public Void visitThisRef(ThisRef ref, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitLocalRef(LocalRef ref, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitClassRef(ClassRef ref, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitDeRef(DeRef ref, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitMemberRef(MemberRef ref, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitBadRef(BadRef ref, IdentificationTable table) {
		return null;
	}

	public ClassDecl currentClass;
	public MethodDecl currentMethod;
}