package miniJava.ContextualAnalyzer;

import java.util.Iterator;

import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
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
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IndexedRef;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.LocalRef;
import miniJava.AbstractSyntaxTrees.MemberRef;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.QualifiedRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementType;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.UnsupportedType;
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
		boolean foundMainMethod = false;

		for (ClassDecl cd : prog.classDeclList) {
			Utilities.addDeclaration(table, cd);

			// The only static method allowed is
			// public static void main(String[])
			for (MethodDecl md : cd.methodDeclList) {
				if (md.isStatic) {
					if (!foundMainMethod && !md.isPrivate && md.id.spelling.equals("main")
							&& md.parameterDeclList.size() == 1) {
						ParameterDecl param = md.parameterDeclList.get(0);
						if (Utilities.getTypeEquivalence(param.type, ArrayType.STRING_ARRAY_TYPE)) {
							foundMainMethod = true;
						}
					}

					if (!foundMainMethod)
						Utilities.reportError("There should be exactly one static method", md.posn);
				}
			}
		}

		if (!foundMainMethod)
			Utilities.reportError("Did not find a method with signature public static void main(String[])", prog.posn);

		return null;
	}

	/**
	 * This is called from the ASTReplaceReference visitor, which adds member
	 * declarations to the identification table of the given class
	 */
	@Override
	public Void visitClassDecl(ClassDecl cd, IdentificationTable table) {
		table.openScope();

		Iterator<FieldDecl> itf = cd.fieldDeclList.iterator();
		while (itf.hasNext())
			itf.next().visit(this, table);

		Iterator<MethodDecl> itm = cd.methodDeclList.iterator();
		while (itm.hasNext())
			itm.next().visit(this, table);

		return null;
	}

	@Override
	public Void visitFieldDecl(FieldDecl fd, IdentificationTable table) {
		if (fd.isStatic) {
			Utilities.reportError("Static fields are not allowed", fd.posn);
		} else {
			if (fd.type.typeKind == TypeKind.VOID) {
				Utilities.reportError("void is an invalid type for the field " + fd.id.spelling, fd.posn);
			} else {
				Utilities.addDeclaration(table, fd);
			}
		}
		return null;
	}

	@Override
	public Void visitMethodDecl(MethodDecl md, IdentificationTable table) {
		Utilities.addDeclaration(table, md);

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

		table.closeScope();

		table.closeScope();

		return null;
	}

	@Override
	public Void visitParameterDecl(ParameterDecl pd, IdentificationTable table) {
		if (pd.type.typeKind == TypeKind.VOID)
			Utilities.reportError("void is an invalid type for the variable " + pd.id.spelling, pd.posn);
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd, IdentificationTable table) {
		if (vd.type.typeKind == TypeKind.VOID)
			Utilities.reportError("void is an invalid type for the variable " + vd.id.spelling, vd.posn);
		return null;
	}

	@Override
	public Void visitBaseType(BaseType type, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitClassType(ClassType type, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType type, IdentificationTable table) {
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
	public Void visitUnsupportedType(UnsupportedType type, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitBlockStmt(BlockStmt stmt, IdentificationTable table) {
		// nested scope
		table.openScope();

		Iterator<Statement> it = stmt.sl.iterator();
		while (it.hasNext())
			it.next().visit(this, table);

		table.closeScope();

		return null;
	}

	@Override
	public Void visitVardeclStmt(VarDeclStmt stmt, IdentificationTable table) {
		stmt.varDecl.visit(this, table);
		return null;
	}

	@Override
	public Void visitAssignStmt(AssignStmt stmt, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitCallStmt(CallStmt stmt, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitIfStmt(IfStmt stmt, IdentificationTable table) {
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
		if (stmt.body instanceof VarDeclStmt) {
			Utilities.reportError("Variable declaration cannot be the only statement in a while statement",
					stmt.body.posn);
		} else {
			stmt.body.visit(this, table);
		}

		return null;
	}

	@Override
	public Void visitUnaryExpr(UnaryExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitBinaryExpr(BinaryExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitRefExpr(RefExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitCallExpr(CallExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitLiteralExpr(LiteralExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitNewObjectExpr(NewObjectExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitNewArrayExpr(NewArrayExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitQualifiedRef(QualifiedRef ref, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitIndexedRef(IndexedRef ref, IdentificationTable table) {
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
	public Void visitBooleanLiteral(BooleanLiteral bool, IdentificationTable table) {
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
}
