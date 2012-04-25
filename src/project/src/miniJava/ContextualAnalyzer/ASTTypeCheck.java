package miniJava.ContextualAnalyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import miniJava.AbstractSyntaxTrees.ExprList;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IndexedRef;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.LocalDecl;
import miniJava.AbstractSyntaxTrees.LocalRef;
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
import miniJava.AbstractSyntaxTrees.Type;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.UnsupportedType;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;

public class ASTTypeCheck implements Visitor<Type, Type> {
	// Table containing predefined and class level declarations
	IdentificationTable table;

	public ASTTypeCheck(IdentificationTable table) {
		this.table = table;
	}

	public MethodDecl typeCheck(Package prog) {
		MethodDecl mainMethod = null;

		visitPackage(prog, null);

		// The only static method allowed is
		// public static void main(String[])
		for (ClassDecl cd : prog.classDeclList) {
			for (OverloadedMethodDecl omd : cd.methodDeclList) {
				for (MethodDecl md : omd) {
					if (md.isStatic) {
						if (mainMethod == null && md.type.isEqualTo(BaseType.VOID_TYPE) && !md.isPrivate
								&& md.id.spelling.equals("main") && md.parameterDeclList.size() == 1) {
							ParameterDecl param = md.parameterDeclList.get(0);
							if (Utilities.getTypeEquivalence(param.type, ArrayType.STRING_ARRAY_TYPE, false)) {
								mainMethod = md;
							}
						} else {
							Utilities.reportError(md.id + " is not allowed to be a static method", md.posn);
						}
					}
				}
			}
		}

		if (mainMethod == null)
			Utilities.reportError("Did not find a method with signature public static void main(String[])", prog.posn);

		return mainMethod;
	}

	@Override
	public Type visitPackage(Package prog, Type type) {
		for (ClassDecl cd : prog.classDeclList)
			cd.visit(this, null);

		return null;
	}

	@Override
	public Type visitClassDecl(ClassDecl cd, Type type) {
		cd.makeClassObject();
		currentClass = cd;

		for (FieldDecl fd : cd.fieldDeclList)
			fd.visit(this, null);

		for (OverloadedMethodDecl omd : cd.methodDeclList)
			omd.visit(this, null);

		// Also visit the constructors
		cd.constructorDecl.visit(this, null);

		return null;
	}

	@Override
	public Type visitFieldDecl(FieldDecl fd, Type type) {
		if (fd.initExpr != null) {
			Type initExprType = fd.initExpr.visit(this, null);
			Utilities.validateTypeEquivalence(fd.type, initExprType, false, fd.posn);
		}

		return null;
	}

	private void checkDuplicateMethods(OverloadedMethodDecl omd) {
		// Check for duplicates by creating a temporary overloaded method and
		// asking for methods matching with each new method being added
		OverloadedMethodDecl temp = new OverloadedMethodDecl(omd);
		for (MethodDecl md : omd) {
			List<Type> argTypes = new ArrayList<Type>();
			for (ParameterDecl pd : md.parameterDeclList)
				argTypes.add(pd.type);

			MethodDecl otherMethod = temp.getMatchingMethodDecl(argTypes);
			if (otherMethod != null) {
				Utilities.reportError("Duplicate method " + md.id + " at " + otherMethod.posn, md.posn);
			}

			temp.add(md);
		}
	}

	@Override
	public Type visitOverloadedMethodDecl(OverloadedMethodDecl omd, Type arg) {
		checkDuplicateMethods(omd);

		for (MethodDecl md : omd)
			md.visit(this, null);

		return null;
	}

	@Override
	public Type visitMethodDecl(MethodDecl md, Type type) {
		for (ParameterDecl pd : md.parameterDeclList)
			pd.visit(this, null);

		for (Statement s : md.statementList)
			s.visit(this, null);

		Type returnType = (md.returnExp == null) ? BaseType.VOID_TYPE : md.returnExp.visit(this, null);

		if (!Utilities.getTypeEquivalence(md.type, returnType, false)) {
			Utilities.reportError("Method " + md.id.spelling + " must return a result of type " + md.type, md.posn);
		}

		return null;
	}

	@Override
	public Type visitParameterDecl(ParameterDecl pd, Type type) {
		return null;
	}

	@Override
	public Type visitVarDecl(VarDecl decl, Type type) {
		return null;
	}

	@Override
	public Type visitBaseType(BaseType type, Type arg) {
		return null;
	}

	@Override
	public Type visitClassType(ClassType type, Type arg) {
		return null;
	}

	@Override
	public Type visitArrayType(ArrayType type, Type arg) {
		return null;
	}

	@Override
	public Type visitStatementType(StatementType type, Type arg) {
		return null;
	}

	@Override
	public Type visitErrorType(ErrorType type, Type arg) {
		return null;
	}

	@Override
	public Type visitUnsupportedType(UnsupportedType type, Type arg) {
		return null;
	}

	@Override
	public Type visitBlockStmt(BlockStmt stmt, Type arg) {
		Iterator<Statement> it = stmt.sl.iterator();
		while (it.hasNext())
			it.next().visit(this, null);

		return null;
	}

	@Override
	public Type visitVardeclStmt(VarDeclStmt stmt, Type arg) {
		if (stmt.initExp != null) {
			Type expType = stmt.initExp.visit(this, null);
			Utilities.validateTypeEquivalence(stmt.varDecl.type, expType, false, stmt.posn);
		}
		return null;
	}

	@Override
	public Type visitAssignStmt(AssignStmt stmt, Type arg) {
		Type refType = stmt.ref.visit(this, null);

		Declaration lhs = stmt.ref.getDeclaration();
		if (!(lhs instanceof FieldDecl || lhs instanceof LocalDecl)) {
			Utilities.reportError(stmt.ref + " cannot be resolved to a variable", stmt.posn);
		}

		Type valType = stmt.val.visit(this, null);
		Utilities.validateTypeEquivalence(refType, valType, false, stmt.posn);

		return null;
	}

	@Override
	public Type visitIfStmt(IfStmt stmt, Type arg) {
		Type conditionType = stmt.cond.visit(this, null);
		if (!Utilities.getTypeEquivalence(conditionType, BaseType.BOOLEAN_TYPE, false)) {
			Utilities.reportError("Type mismatch: Cannot convert " + conditionType + " to boolean", stmt.cond.posn);
			return new ErrorType(stmt.cond.posn);
		}
		stmt.thenStmt.visit(this, null);
		if (stmt.elseStmt != null)
			stmt.elseStmt.visit(this, null);
		return new StatementType(stmt.toString(), stmt.posn);
	}

	@Override
	public Type visitWhileStmt(WhileStmt stmt, Type arg) {
		if (stmt.cond.visit(this, null).typeKind != TypeKind.BOOLEAN) {
			Utilities.reportError("Type mismatch: Cannot convert " + stmt.cond + " to boolean", stmt.cond.posn);
			return new ErrorType(stmt.cond.posn);
		}
		stmt.body.visit(this, null);

		return new StatementType(stmt.toString(), stmt.posn);
	}

	@Override
	public Type visitUnaryExpr(UnaryExpr expr, Type arg) {
		return expr.expr.visit(this, null);
	}

	@Override
	public Type visitBinaryExpr(BinaryExpr expr, Type arg) {

		Type leftType = expr.left.visit(this, null);
		Type rightType = expr.right.visit(this, null);
		Type resultType = null;

		switch (expr.operator.operatorType) {

		case EQUALTO_EQUALTO:
		case BANG_EQUALTO:
			if (!Utilities.validateTypeEquivalence(leftType, rightType, true, expr.posn)) {
				return new ErrorType(expr.posn);
			}
			resultType = new BaseType(TypeKind.BOOLEAN, "boolean", expr.posn);
			break;

		case PIPE_PIPE:
		case AMPERSAND_AMPERSAND:
			if (!Utilities.validateTypeEquivalence(BaseType.BOOLEAN_TYPE, leftType, false, expr.left.posn)) {
				return new ErrorType(expr.left.posn);
			}
			if (!Utilities.validateTypeEquivalence(BaseType.BOOLEAN_TYPE, rightType, false, expr.right.posn)) {
				return new ErrorType(expr.right.posn);
			}
			resultType = new BaseType(TypeKind.BOOLEAN, "boolean", expr.posn);
			break;

		case PLUS:
		case MINUS:
		case ASTERISK:
		case SLASH:
			if (!Utilities.validateTypeEquivalence(BaseType.INT_TYPE, leftType, false, expr.left.posn)) {
				return new ErrorType(expr.left.posn);
			}
			if (!Utilities.validateTypeEquivalence(BaseType.INT_TYPE, rightType, false, expr.right.posn)) {
				return new ErrorType(expr.right.posn);
			}
			resultType = new BaseType(TypeKind.INT, "int", expr.posn);
			break;

		case LANGLE_EQUALTO:
		case RANGLE_EQUALTO:
		case LANGLE:
		case RANGLE:
			if (!Utilities.validateTypeEquivalence(BaseType.INT_TYPE, leftType, false, expr.left.posn)) {
				return new ErrorType(expr.left.posn);
			}
			if (!Utilities.validateTypeEquivalence(BaseType.INT_TYPE, rightType, false, expr.right.posn)) {
				return new ErrorType(expr.right.posn);
			}
			resultType = new BaseType(TypeKind.BOOLEAN, "boolean", expr.posn);
			break;

		default:
			// throw new
			// RuntimeException("A wild operator appeared! Abort! Abort!");
			return null;
		}

		return resultType;
	}

	@Override
	public Type visitRefExpr(RefExpr expr, Type arg) {
		return Utilities.handleUnsupportedType(expr.ref.visit(this, null), table);
	}

	private Type validateMethodReference(Reference methodRef, ExprList argList) {
		Declaration decl = methodRef.getDeclaration();

		if (!(decl instanceof OverloadedMethodDecl)) {
			Utilities.reportError("Method " + methodRef + " is undefined", methodRef.posn);
			return new ErrorType(methodRef.posn);
		}

		List<Type> argTypes = new ArrayList<Type>();
		for (Expression e : argList) {
			argTypes.add(e.visit(this, null));
		}

		MethodDecl methodDecl = ((OverloadedMethodDecl) decl).getMatchingMethodDecl(argTypes);

		if (methodDecl == null) {
			Utilities.reportError("The method " + decl.id.spelling + " is not applicable to the arguments provided",
					methodRef.posn);
			return new ErrorType(methodRef.posn);
		}

		if (methodDecl.isStatic) {
			// Static methods cannot be invoked
			Utilities.reportError("Static method " + methodDecl.id.spelling + " cannot be invoked", methodRef.posn);
			return new ErrorType(methodRef.posn);
		}

		if (methodDecl.isPrivate && currentClass != methodDecl.parentClass) {
			// Static methods cannot be invoked
			Utilities.reportError("Private method " + methodDecl.id.spelling + " is not accessible here",
					methodRef.posn);
			return new ErrorType(methodRef.posn);
		}

		methodRef.setDeclaration(methodDecl);

		return Utilities.handleUnsupportedType(methodDecl.type, table);
	}

	@Override
	public Type visitCallStmt(CallStmt stmt, Type arg) {
		return validateMethodReference(stmt.methodRef, stmt.argList);
	}

	@Override
	public Type visitCallExpr(CallExpr expr, Type arg) {
		return validateMethodReference(expr.functionRef, expr.argList);
	}

	@Override
	public Type visitLiteralExpr(LiteralExpr expr, Type arg) {
		return expr.literal.visit(this, null);
	}

	@Override
	public Type visitNewObjectExpr(NewObjectExpr expr, Type arg) {
		List<Type> argTypes = new ArrayList<Type>();

		for (Expression e : expr.argList)
			argTypes.add(e.visit(this, null));

		expr.matchedConstructor = expr.classtype.declaration.constructorDecl.getMatchingMethodDecl(argTypes);

		if (expr.matchedConstructor == null) {
			Utilities.reportError("No matching constructor for class " + expr.classtype.declaration.id, expr.posn);
			return new ErrorType(expr.posn);
		}

		if (expr.matchedConstructor.isPrivate && currentClass != expr.matchedConstructor.parentClass) {
			Utilities.reportError("Cannot access the matching constructor for " + expr.classtype.declaration.id,
					expr.posn);
			return new ErrorType(expr.posn);
		}

		return Utilities.handleUnsupportedType(expr.classtype, table);
	}

	@Override
	public Type visitNewArrayExpr(NewArrayExpr expr, Type arg) {
		// Size should be an integer
		Type sizeExprType = expr.sizeExpr.visit(this, null);
		if (sizeExprType.typeKind != TypeKind.INT) {
			Utilities.reportError("Type mismatch: Cannot convert from " + sizeExprType + " to int", expr.sizeExpr.posn);
			return new ErrorType(expr.sizeExpr.posn);
		}

		return new ArrayType(expr.eltType, expr.eltType.spelling, expr.eltType.posn);
	}

	@Override
	public Type visitQualifiedRef(QualifiedRef ref, Type arg) {
		// This should not ever get visited
		// throw new RuntimeException("Here be Dragons!");
		return null;
	}

	@Override
	public Type visitIndexedRef(IndexedRef ref, Type arg) {
		// Index should be an integer
		Type indexType = ref.indexExpr.visit(this, null);
		if (indexType.typeKind != TypeKind.INT) {
			Utilities.reportError("Type mismatch: Cannot convert from " + indexType + " to int", ref.indexExpr.posn);
			return new ErrorType(ref.indexExpr.posn);
		}

		// Reference must be an array type
		Type refType = ref.ref.visit(this, null);
		if (!(refType instanceof ArrayType)) {
			Utilities.reportError("The type of " + ref.ref + " must be an array type but it resolved to " + refType,
					ref.ref.posn);
			return new ErrorType(ref.posn);
		}

		return Utilities.handleUnsupportedType(((ArrayType) refType).eltType, table);
	}

	@Override
	public Type visitIdentifier(Identifier id, Type arg) {
		return Utilities.handleUnsupportedType(id.declaration.type, table);
	}

	@Override
	public Type visitOperator(Operator op, Type arg) {
		return null;
	}

	@Override
	public Type visitIntLiteral(IntLiteral num, Type arg) {
		return new BaseType(TypeKind.INT, "int", num.posn);
	}

	@Override
	public Type visitBooleanLiteral(BooleanLiteral bool, Type arg) {
		return new BaseType(TypeKind.BOOLEAN, "boolean", bool.posn);
	}

	@Override
	public Type visitStringLiteral(StringLiteral str, Type arg) {
		return IdentificationTable.STRING_DECL.type;
	}

	@Override
	public Type visitThisRef(ThisRef ref, Type arg) {
		return Utilities.handleUnsupportedType(ref.identifier.declaration.type, table);
	}

	@Override
	public Type visitLocalRef(LocalRef ref, Type arg) {
		return Utilities.handleUnsupportedType(ref.identifier.declaration.type, table);
	}

	@Override
	public Type visitClassRef(ClassRef ref, Type arg) {
		return Utilities.handleUnsupportedType(ref.identifier.declaration.type, table);
	}

	@Override
	public Type visitDeRef(DeRef ref, Type arg) {
		return ref.memberReference.visit(this, null);
	}

	@Override
	public Type visitMemberRef(MemberRef ref, Type arg) {
		return Utilities.handleUnsupportedType(ref.identifier.declaration.type, table);
	}

	@Override
	public Type visitBadRef(BadRef ref, Type arg) {
		return new ErrorType(ref.posn);
	}

	public ClassDecl currentClass;
}