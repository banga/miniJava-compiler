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
import miniJava.AbstractSyntaxTrees.ErrorType;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IndexedRef;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.LocalDecl;
import miniJava.AbstractSyntaxTrees.LocalRef;
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MemberRef;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.OverloadedMethodDecl;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.QualifiedRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Reference;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementType;
import miniJava.AbstractSyntaxTrees.StringLiteral;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.UnsupportedType;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;

/**
 * Replaces QualifiedReference with a proper tree of references
 */
public class ASTReplaceReference implements Visitor<IdentificationTable, AST> {
	@Override
	public AST visitPackage(Package prog, IdentificationTable table) {
		for (ClassDecl cd : prog.classDeclList)
			cd.visit(this, table);
		return null;
	}

	@Override
	public AST visitClassDecl(ClassDecl cd, IdentificationTable table) {
		cd.id.declaration = cd;
		currentClass = cd;

		// Populate the identification table with the members of this class
		ASTIdentifyMembers identify = new ASTIdentifyMembers();
		cd.visit(identify, table);

		for (FieldDecl fd : cd.fieldDeclList)
			fd.visit(this, table);

		for (OverloadedMethodDecl md : cd.methodDeclList)
			md.visit(this, table);

		// Also visit the constructors
		cd.constructorDecl.visit(this, table);

		table.closeScope();

		return null;
	}

	@Override
	public AST visitFieldDecl(FieldDecl fd, IdentificationTable table) {
		if (fd.initExpr != null)
			fd.initExpr.visit(this, table);

		return null;
	}

	@Override
	public AST visitOverloadedMethodDecl(OverloadedMethodDecl omd, IdentificationTable table) {
		for (MethodDecl md : omd)
			md.visit(this, table);

		return null;
	}

	@Override
	public AST visitMethodDecl(MethodDecl md, IdentificationTable table) {
		// Parameter scope
		table.openScope();

		for (ParameterDecl pd : md.parameterDeclList)
			pd.visit(this, table);

		// Scope of local variables
		table.openScope();

		for (Statement st : md.statementList)
			st.visit(this, table);

		if (md.returnExp != null)
			md.returnExp.visit(this, table);

		table.closeScope();

		table.closeScope();

		return null;
	}

	@Override
	public AST visitParameterDecl(ParameterDecl pd, IdentificationTable table) {
		Utilities.addDeclaration(table, pd);
		return null;
	}

	@Override
	public AST visitVarDecl(VarDecl vd, IdentificationTable table) {
		Utilities.addDeclaration(table, vd);
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

		for (Statement s : stmt.sl)
			s.visit(this, table);

		table.closeScope();

		return null;
	}

	@Override
	public AST visitVardeclStmt(VarDeclStmt stmt, IdentificationTable table) {
		stmt.varDecl.visit(this, table);
		stmt.initExp.visit(this, table);
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
		for (Expression e : stmt.argList)
			e.visit(this, table);

		return null;
	}

	@Override
	public AST visitIfStmt(IfStmt stmt, IdentificationTable table) {
		stmt.cond.visit(this, table);
		stmt.thenStmt.visit(this, table);
		if (stmt.elseStmt != null)
			stmt.elseStmt.visit(this, table);

		return null;
	}

	@Override
	public AST visitWhileStmt(WhileStmt stmt, IdentificationTable table) {
		stmt.cond.visit(this, table);
		stmt.body.visit(this, table);

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
		for (Expression e : expr.argList)
			e.visit(this, table);

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

		for (Expression e : expr.argList)
			e.visit(this, table);

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

			if (id.declaration instanceof ClassDecl) {
				newRef = new ClassRef(id, id.posn);
			} else if (id.declaration instanceof MemberDecl) {
				newRef = new MemberRef(id, id.posn);
			} else if (id.declaration instanceof LocalDecl || id.declaration instanceof ParameterDecl) {
				newRef = new LocalRef(id, id.posn);
			} else {
				Utilities.reportError("Undeclared identifier '" + id + "'", id.posn);
				return new BadRef(id, id.posn);
			}
		}

		// Handle subsequent Identifiers in the QualifiedRef list
		for (int i = (isThisRef) ? 0 : 1; i < qRef.qualifierList.size(); i++) {
			id = qRef.qualifierList.get(i);

			// Identification failed on id, so it's a bad reference
			if (id.declaration == null) {
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
	public AST visitStringLiteral(StringLiteral bool, IdentificationTable arg) {
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

	public ClassDecl currentClass;
}
