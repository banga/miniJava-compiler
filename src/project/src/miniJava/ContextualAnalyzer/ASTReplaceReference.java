package miniJava.ContextualAnalyzer;

import java.util.Iterator;

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
import miniJava.AbstractSyntaxTrees.ErrorType;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
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
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.QualifiedRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Reference;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementType;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.UnsupportedType;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;

/**
 * Replaces QualifiedReference with a proper tree of references; Checks if
 * declarations belong to valid types in the identification table
 */
public class ASTReplaceReference implements Visitor<IdentificationTable, AST> {

	public ClassDecl currentClass = null;
	public MethodDecl currentMethod = null;

	@Override
	public AST visitPackage(Package prog, IdentificationTable table) {
		Iterator<ClassDecl> it = prog.classDeclList.iterator();
		while (it.hasNext()) {
			ClassDecl cd = it.next();
			cd.visit(this, table);
		}
		return null;
	}

	@Override
	public AST visitClassDecl(ClassDecl cd, IdentificationTable table) {
		int classScope = table.linkDeclaration(cd.id);
		if (classScope == IdentificationTable.INVALID_SCOPE) {
			// Shouldn't happen
			//throw new RuntimeException("Couldn't match class " + cd.id + " to its declaration");
			return null;
		}

		currentClass = cd;

		// Populate the identification table with the members of this class
		ASTIdentifyMembers identify = new ASTIdentifyMembers();
		cd.visit(identify, table);

		Iterator<FieldDecl> itf = cd.fieldDeclList.iterator();
		while (itf.hasNext())
			itf.next().visit(this, table);

		Iterator<MethodDecl> itm = cd.methodDeclList.iterator();
		while (itm.hasNext())
			itm.next().visit(this, table);

		table.closeScope();

		return null;
	}

	@Override
	public AST visitFieldDecl(FieldDecl fd, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitMethodDecl(MethodDecl md, IdentificationTable table) {
		currentMethod = md;

		// Parameter scope
		table.openScope();

		Iterator<ParameterDecl> itp = md.parameterDeclList.iterator();
		while (itp.hasNext())
			itp.next().visit(this, table);

		// Scope of local variables
		table.openScope();

		Iterator<Statement> its = md.statementList.iterator();
		while (its.hasNext())
			its.next().visit(this, table);

		if (md.returnExp != null)
			md.returnExp.visit(this, table);

		table.closeScope();

		table.closeScope();

		return null;
	}

	@Override
	public AST visitParameterDecl(ParameterDecl pd, IdentificationTable table) {
		// The Identification table is complete, so now we can check for
		// parameter types
		Utilities.validateType(pd.type, table);
		Utilities.addDeclaration(table, pd);
		return null;
	}

	@Override
	public AST visitVarDecl(VarDecl decl, IdentificationTable table) {
		Utilities.validateType(decl.type, table);
		return null;
	}

	@Override
	public AST visitBaseType(BaseType type, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitClassType(ClassType type, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitArrayType(ArrayType type, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitStatementType(StatementType type, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitErrorType(ErrorType type, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitUnsupportedType(UnsupportedType type, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitBlockStmt(BlockStmt stmt, IdentificationTable table) {
		// nested scope
		table.openScope();

		Iterator<Statement> it = stmt.sl.iterator();
		while (it.hasNext())
			it.next().visit(this, table);

		table.closeScope();
		return null;
	}

	@Override
	public AST visitVardeclStmt(VarDeclStmt stmt, IdentificationTable table) {
		stmt.varDecl.visit(this, table);
		stmt.initExp.visit(this, table);
		Utilities.addDeclaration(table, stmt.varDecl);
		return null;
	}

	@Override
	public AST visitAssignStmt(AssignStmt stmt, IdentificationTable table) {
		stmt.ref = (Reference) stmt.ref.visit(this, table);
		stmt.val.visit(this, table);
		return null;
	}

	@Override
	public AST visitCallStmt(CallStmt stmt, IdentificationTable table) {
		stmt.methodRef = (Reference) stmt.methodRef.visit(this, table);
		Iterator<Expression> itf = stmt.argList.iterator();
		while (itf.hasNext())
			itf.next().visit(this, table);
		return null;
	}

	@Override
	public AST visitIfStmt(IfStmt stmt, IdentificationTable table) {
		stmt.cond.visit(this, table);
		if (stmt.thenStmt instanceof VarDeclStmt) {
			Utilities.reportError("Variable declaration cannot be the only statement in a conditional statement at ",
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
	public AST visitWhileStmt(WhileStmt stmt, IdentificationTable table) {
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
	public AST visitUnaryExpr(UnaryExpr expr, IdentificationTable table) {
		expr.operator.visit(this, table);
		expr.expr.visit(this, table);
		return null;
	}

	@Override
	public AST visitBinaryExpr(BinaryExpr expr, IdentificationTable table) {
		expr.operator.visit(this, table);
		expr.left.visit(this, table);
		expr.right.visit(this, table);
		return null;
	}

	@Override
	public AST visitRefExpr(RefExpr expr, IdentificationTable table) {
		expr.ref = (Reference) expr.ref.visit(this, table);
		return null;
	}

	@Override
	public AST visitCallExpr(CallExpr expr, IdentificationTable table) {
		expr.functionRef = (Reference) expr.functionRef.visit(this, table);
		Iterator<Expression> itf = expr.argList.iterator();
		while (itf.hasNext())
			itf.next().visit(this, table);
		return null;
	}

	@Override
	public AST visitLiteralExpr(LiteralExpr expr, IdentificationTable table) {
		expr.literal.visit(this, table);
		return null;
	}

	@Override
	public AST visitNewObjectExpr(NewObjectExpr expr, IdentificationTable table) {
		expr.classtype.visit(this, table);
		return null;
	}

	@Override
	public AST visitNewArrayExpr(NewArrayExpr expr, IdentificationTable table) {
		expr.eltType.visit(this, table);
		expr.sizeExpr.visit(this, table);
		return null;
	}

	@Override
	public AST visitQualifiedRef(QualifiedRef qRef, IdentificationTable table) {
		Reference newRef = null;
		Identifier id;
		boolean isThisRef = false;

		if (qRef.thisRelative) {
			newRef = new ThisRef(currentClass.id, currentClass.id.posn);
			isThisRef = true;
		} else {
			id = qRef.qualifierList.get(0);

			// First look for a member of the current class
			id.declaration = currentClass.getMemberDeclaration(id.spelling);

			if (id.declaration != null) {
				// Non-static members cannot be accessed from static methods
				if (currentMethod.isStatic) {
					MemberDecl md = (MemberDecl) id.declaration;
					if (!md.isStatic) {
						Utilities.reportError("Non-static member " + id + " cannot be accessed from static method "
								+ currentMethod.id, id.posn);
						return new BadRef(id, id.posn);
					}
				}

				newRef = new MemberRef(id, id.posn);
			} else {
				int scope = table.linkDeclaration(id);

				if (scope == IdentificationTable.PREDEFINED_SCOPE || scope == IdentificationTable.CLASS_SCOPE) {
					newRef = new ClassRef(id, id.posn);
				} else if (scope == IdentificationTable.LOCAL_SCOPE || scope == IdentificationTable.PARAMETER_SCOPE) {
					newRef = new LocalRef(id, id.posn);
				} else {
					Utilities.reportError("Undeclared identifier '" + id + "'", id.posn);
					return new BadRef(id, id.posn);
				}
			}
		}

		// Handle subsequent Identifiers in the QualifiedRef list
		for (int i = (isThisRef) ? 0 : 1; i < qRef.qualifierList.size(); i++) {
			boolean currentIdentifierIsThis = (i == 0);
			// Identifiers indexed > 0 in the QualifiedRef list are always
			// Members of Class. Get the parent to which the
			// current Identifier is a member of.
			Identifier parentID = (currentIdentifierIsThis) ? currentClass.id : qRef.qualifierList.get(i - 1);
			boolean isParentClassName = parentID.declaration instanceof ClassDecl;

			if (!(parentID.declaration.type instanceof ClassType)) {
				Utilities.reportError(parentID + " is not an instance or a class", parentID.posn);
				return new BadRef(parentID, parentID.posn);
			}

			ClassType parentClassType = (ClassType) parentID.declaration.type;
			table.linkDeclaration(parentClassType.name);
			ClassDecl parentClassDecl = (ClassDecl) parentClassType.name.declaration;

			id = qRef.qualifierList.get(i);
			id.declaration = parentClassDecl.getFieldDeclaration(id.spelling);

			if (id.declaration == null) {
				if (i == qRef.qualifierList.size() - 1) {
					// Could be a method if this is the last qualifier
					id.declaration = parentClassDecl.getMethodDeclaration(id.spelling);
					if (id.declaration == null) {
						Utilities.reportError(id + " is not a member of " + parentClassDecl.id, id.posn);
						return new BadRef(id, id.posn);
					}
				} else {
					Utilities.reportError(id + " is not a field of " + parentClassDecl.id, id.posn);
					return new BadRef(id, id.posn);
				}
			}

			MemberDecl memberDecl = (MemberDecl) id.declaration;

			// Cannot use the member of a class in "Class.Member" form if it is
			// not static
			if (isParentClassName && !currentIdentifierIsThis && !memberDecl.isStatic) {
				Utilities.reportError(id + " is not a static member of " + parentClassDecl.id, id.posn);
				return new BadRef(id, id.posn);
			}

			// Cannot use the member of a class if it is not public
			if (memberDecl.isPrivate) {
				Utilities.reportError(id + " is not a public member of " + parentClassDecl.id, id.posn);
				return new BadRef(id, id.posn);
			}

			// Static members can only be referenced through their fully
			// qualified names, starting with class name
			if (memberDecl.isStatic && !isParentClassName) {
				Utilities.reportError("Static member " + id + " can only be referenced through its parent class name", id.posn);
				return new BadRef(id, id.posn);
			}

			newRef = new DeRef(newRef, new MemberRef(id, id.posn), qRef.posn);
		}

		return newRef;
	}

	@Override
	public AST visitIndexedRef(IndexedRef ref, IdentificationTable table) {
		ref.ref = (Reference) ref.ref.visit(this, table);
		ref.indexExpr.visit(this, table);
		return ref;
	}

	@Override
	public AST visitIdentifier(Identifier id, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitOperator(Operator op, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitIntLiteral(IntLiteral num, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitBooleanLiteral(BooleanLiteral bool, IdentificationTable table) {
		return null;
	}

	@Override
	public AST visitThisRef(ThisRef ref, IdentificationTable arg) {
		return null;
	}

	@Override
	public AST visitLocalRef(LocalRef ref, IdentificationTable arg) {
		return null;
	}

	@Override
	public AST visitClassRef(ClassRef ref, IdentificationTable arg) {
		return null;
	}

	@Override
	public AST visitDeRef(DeRef ref, IdentificationTable arg) {
		return null;
	}

	@Override
	public AST visitMemberRef(MemberRef ref, IdentificationTable arg) {
		return null;
	}

	@Override
	public AST visitBadRef(BadRef ref, IdentificationTable arg) {
		return null;
	}
}
