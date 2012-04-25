package miniJava.CodeGenerator;

import java.util.HashMap;

import mJAM.Machine;
import mJAM.Machine.Op;
import mJAM.Machine.Prim;
import mJAM.Machine.Reg;
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
import miniJava.AbstractSyntaxTrees.OverloadedMethodDecl;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.QualifiedRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.StatementType;
import miniJava.AbstractSyntaxTrees.StringLiteral;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.UnsupportedType;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.ContextualAnalyzer.IdentificationTable;

public class ASTGenerateCode implements Visitor<Object, Void> {

	enum FetchType {
		ADDRESS, VALUE, METHOD
	};

	// Displacement of class objects from SB
	int classObjectDisplacement;

	// Displacement of local variables from LB
	int localDisplacement;
	HashMap<Integer, MethodRuntimeEntity> methodDisplacements = new HashMap<Integer, MethodRuntimeEntity>();

	/**
	 * Calculates the correct class object displacement without emitting code
	 * 
	 * @param cd
	 */
	private void calculateClassObjectDisplacement(ClassDecl cd) {
		if (cd.runtimeEntity.classObjectDisplacement != -1)
			return;

		cd.runtimeEntity.classObjectDisplacement = 0;
		if (cd.superClass != null) {
			calculateClassObjectDisplacement(cd.superClass.declaration);
			cd.runtimeEntity.classObjectDisplacement = cd.superClass.declaration.runtimeEntity.classObjectDisplacement;
		}

		cd.runtimeEntity.classObjectDisplacement = classObjectDisplacement;
		classObjectDisplacement += cd.classObject.size() + 2;
	}

	/**
	 * Emit code for class object creation if not emitted already
	 * 
	 * @param cd
	 */
	private void emitClassObject(ClassDecl cd) {
		if (cd.runtimeEntity.isClassObjectEmitted)
			return;

		// First emit the super class
		if (cd.superClass != null)
			emitClassObject(cd.superClass.declaration);

		if (cd.superClass != null) {
			Machine.emit(Op.LOADA, Reg.SB, cd.superClass.declaration.runtimeEntity.classObjectDisplacement);
		} else {
			Machine.emit(Op.LOADL, -1);
		}

		Machine.emit(Op.LOADL, cd.classObject.size());
		for (MethodDecl md : cd.classObject) {
			Machine.emit(Op.LOADA, Reg.CB, md.runtimeEntity.displacement);
		}

		cd.runtimeEntity.isClassObjectEmitted = true;
	}

	@Override
	public Void visitPackage(Package prog, Object arg) {
		Machine.initCodeGen();

		// Pre-calculate class object displacements, field displacements etc.
		classObjectDisplacement = 0;
		for (ClassDecl cd : prog.classDeclList) {
			calculateClassObjectDisplacement(cd);
			cd.populateRuntimeEntities();
		}

		// Jump to class object creation
		int classObjectJumpAddress = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP, Reg.CB, 0);

		// Generate code for all methods
		for (ClassDecl cd : prog.classDeclList)
			cd.visit(this, null);

		// Patch method calls
		for (Integer addr : methodDisplacements.keySet())
			Machine.patch(addr, methodDisplacements.get(addr).displacement);

		// Patch jump to class object creation
		Machine.patch(classObjectJumpAddress, Machine.nextInstrAddr());

		// Create class objects
		for (ClassDecl cd : prog.classDeclList) {
			emitClassObject(cd);
		}

		// Call main method
		Machine.emit(Op.LOADL, 0);
		Machine.emit(Op.LOADL, -1);
		Machine.emit(Op.CALL, Reg.CB, ((MethodDecl) arg).runtimeEntity.displacement);
		Machine.emit(Op.HALT, 0, 0, 0);

		return null;
	}

	@Override
	public Void visitClassDecl(ClassDecl cd, Object arg) {
		for (FieldDecl fd : cd.fieldDeclList) {
			fd.visit(this, null);
		}

		for (OverloadedMethodDecl md : cd.methodDeclList) {
			md.visit(this, null);
		}

		// Also visit the constructors
		cd.constructorDecl.visit(this, null);

		// Create a field initializer function:
		cd.fieldInitializerEntity.displacement = Machine.nextInstrAddr();

		// Initialize fields with initializing expressions
		for (FieldDecl fd : cd.fieldDeclList) {
			if (fd.initExpr != null) {
				Machine.emit(Op.LOADA, Reg.OB, 0); // Load object's address
				Machine.emit(Op.LOADL, fd.runtimeEntity.displacement);
				fd.initExpr.visit(this, null);
				Machine.emit(Prim.fieldupd);
			}
		}
		Machine.emit(Op.RETURN, 0, 0, 0);

		return null;
	}

	@Override
	public Void visitFieldDecl(FieldDecl fd, Object arg) {
		return null;
	}

	@Override
	public Void visitOverloadedMethodDecl(OverloadedMethodDecl omd, Object arg) {
		for (MethodDecl md : omd)
			md.visit(this, null);

		return null;
	}

	private int allocateSpaceForLocals(StatementList sl) {
		int numAllocated = 0;

		for (Statement s : sl) {
			if (s instanceof VarDeclStmt) {
				VarDecl v = ((VarDeclStmt) s).varDecl;
				v.runtimeEntity.displacement = localDisplacement + numAllocated;
				numAllocated++;
			}
		}

		if (numAllocated > 0) {
			Machine.emit(Op.PUSH, numAllocated);
			localDisplacement += numAllocated;
		}

		return numAllocated;
	}

	@Override
	public Void visitMethodDecl(MethodDecl md, Object arg) {
		localDisplacement = 3;
		md.runtimeEntity.displacement = Machine.nextInstrAddr();

		int d = -md.parameterDeclList.size();
		for (ParameterDecl pd : md.parameterDeclList) {
			pd.visit(this, null);
			pd.runtimeEntity.displacement = d++;
		}

		int numAllocated = allocateSpaceForLocals(md.statementList);
		for (Statement s : md.statementList) {
			s.visit(this, null);
		}
		if (md.returnExp != null) {
			md.returnExp.visit(this, null);
		}
		localDisplacement -= numAllocated; // We don't POP because RETURN does
											// that automatically

		// Machine.emit(Op.HALT, 4, 0, 0);

		if (md.returnExp != null) {
			Machine.emit(Op.RETURN, 1, 0, md.parameterDeclList.size());
		} else {
			Machine.emit(Op.RETURN, 0, 0, md.parameterDeclList.size());
		}

		return null;
	}

	@Override
	public Void visitBlockStmt(BlockStmt stmt, Object arg) {
		int numAllocated = allocateSpaceForLocals(stmt.sl);
		for (Statement s : stmt.sl)
			s.visit(this, null);
		localDisplacement -= numAllocated;
		Machine.emit(Op.POP, numAllocated);

		return null;
	}

	@Override
	public Void visitParameterDecl(ParameterDecl pd, Object arg) {
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl decl, Object arg) {
		return null;
	}

	@Override
	public Void visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		// The space for this variable has already been allocated by
		// allocateLocalVariables
		// Store the value of the initializing expression in variable
		stmt.initExp.visit(this, FetchType.VALUE);
		Machine.emit(Op.STORE, Reg.LB, stmt.varDecl.runtimeEntity.displacement);

		return null;
	}

	@Override
	public Void visitAssignStmt(AssignStmt stmt, Object arg) {
		// The lhs of an assignment statement can be:
		// 1. A local variable
		// 2. An element of an array, like a[2]. Note that a.b[2] is not allowed
		// 3. A field of an object or of this
		if (stmt.ref instanceof LocalRef) {
			// local variable, stored on the stack
			stmt.val.visit(this, null);
			Machine.emit(Op.STORE, Reg.LB, getLocalRefDisplacement((LocalRef) stmt.ref));
		} else {
			stmt.ref.visit(this, FetchType.ADDRESS);
			stmt.val.visit(this, null);

			if (stmt.ref instanceof IndexedRef) {
				// array element
				Machine.emit(Prim.arrayupd);
			} else {
				// field
				Machine.emit(Prim.fieldupd);
			}
		}

		return null;
	}

	@Override
	public Void visitCallStmt(CallStmt stmt, Object arg) {
		// Get the arguments on the stack
		for (Expression exp : stmt.argList)
			exp.visit(this, null);

		// Handle System.out.println(int x)
		MethodDecl methodDecl = (MethodDecl) stmt.methodRef.getDeclaration();
		if (methodDecl == IdentificationTable.PRINTLN_INT_DECL) {
			Machine.emit(Prim.putint);
			Machine.emit(Prim.puteol);
			return null;
		} else if (methodDecl == IdentificationTable.PRINTLN_STRING_DECL) {
			// String's address is on stack
			Machine.emit(Op.LOADL, 0); // Load counter = 0 on stack

			int testJumpIntrAddr = Machine.nextInstrAddr();
			Machine.emit(Op.JUMP, 0, Reg.CB, 0);

			// LOOP:
			// Stack has counter, base address
			int loopStartAddress = Machine.nextInstrAddr();
			Machine.emit(Op.LOAD, Reg.ST, -2); // Copy string base address
			Machine.emit(Op.LOAD, Reg.ST, -2); // Copy the counter value
			Machine.emit(Prim.arrayref);
			Machine.emit(Prim.put);
			Machine.emit(Prim.succ); // Increase the counter

			Machine.patch(testJumpIntrAddr, Machine.nextInstrAddr());

			Machine.emit(Op.LOAD, Reg.ST, -1); // Copy the counter value
			Machine.emit(Op.LOAD, Reg.ST, -3); // Copy array base address
			Machine.emit(Prim.pred); // Get address of length field
			Machine.emit(Op.LOADI); // Load length of string on stack
			Machine.emit(Prim.lt);
			Machine.emit(Op.JUMPIF, 1, Reg.CB, loopStartAddress);

			Machine.emit(Op.POP, 2); // Pop the string base address and counter
										// value
			Machine.emit(Prim.puteol);

			return null;
		}

		if (stmt.methodRef instanceof MemberRef) {
			// We are calling a method of the current class, so we need to place
			// the current OB on stack. Otherwise, DeRef places the right
			// instance on stack
			Machine.emit(Op.LOADA, Reg.OB, 0);
		}

		// The methodRef generates a CALL statement
		stmt.methodRef.visit(this, FetchType.METHOD);

		// When only called for side-effect, pop any value that may be returned
		if(methodDecl.type.typeKind != TypeKind.VOID)
			Machine.emit(Op.POP, 1);
		
		return null;
	}

	@Override
	public Void visitIfStmt(IfStmt stmt, Object arg) {
		/*
		 * <evaluate condition> JUMPIF 0 ELSE <then block> JUMP END ELSE: <else
		 * block> END:
		 */

		// visit to this expression leaves a boolean (0 or 1)
		stmt.cond.visit(this, null);

		int elseJumpAddr = Machine.nextInstrAddr();
		Machine.emit(Op.JUMPIF, 0, Reg.CB, 0);
		stmt.thenStmt.visit(this, null);

		if (stmt.elseStmt != null) {
			int endJumpAddr = Machine.nextInstrAddr();
			Machine.emit(Op.JUMP, Reg.CB, 0);
			Machine.patch(elseJumpAddr, Machine.nextInstrAddr());
			stmt.elseStmt.visit(this, null);
			Machine.patch(endJumpAddr, Machine.nextInstrAddr());
		} else {
			Machine.patch(elseJumpAddr, Machine.nextInstrAddr());
		}

		return null;
	}

	@Override
	public Void visitWhileStmt(WhileStmt stmt, Object arg) {
		/*
		 * JUMP TEST LOOP: <loop body> TEST: <evaluate condition> JUMPIF 1 LOOP
		 */

		// JUMP TEST
		int testJumpAddr = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP, 0, Reg.CB, 0);
		// LOOP:
		int loopStartAddr = Machine.nextInstrAddr();
		stmt.body.visit(this, null);
		// TEST:
		int testStart = Machine.nextInstrAddr();
		stmt.cond.visit(this, null);
		Machine.emit(Op.JUMPIF, 1, Reg.CB, loopStartAddr);
		Machine.patch(testJumpAddr, testStart);

		return null;
	}

	@Override
	public Void visitUnaryExpr(UnaryExpr expr, Object arg) {
		expr.expr.visit(this, null);

		switch (expr.operator.operatorType) {
		case BANG:
			Machine.emit(Prim.not);
			break;
		case MINUS:
			Machine.emit(Prim.neg);
			break;
		}

		return null;
	}

	@Override
	public Void visitBinaryExpr(BinaryExpr expr, Object arg) {
		expr.left.visit(this, null);
		expr.right.visit(this, null);

		switch (expr.operator.operatorType) {
		case LANGLE:
			Machine.emit(Prim.lt);
			break;
		case RANGLE:
			Machine.emit(Prim.gt);
			break;
		case EQUALTO_EQUALTO:
			Machine.emit(Prim.eq);
			break;
		case LANGLE_EQUALTO:
			Machine.emit(Prim.le);
			break;
		case RANGLE_EQUALTO:
			Machine.emit(Prim.ge);
			break;
		case BANG_EQUALTO:
			Machine.emit(Prim.ne);
			break;
		case PLUS:
			Machine.emit(Prim.add);
			break;
		case MINUS:
			Machine.emit(Prim.sub);
			break;
		case ASTERISK:
			Machine.emit(Prim.mult);
			break;
		case SLASH:
			Machine.emit(Prim.div);
			break;
		case PIPE_PIPE:
			Machine.emit(Prim.or);
			break;
		case AMPERSAND_AMPERSAND:
			Machine.emit(Prim.and);
			break;
		}

		return null;
	}

	@Override
	public Void visitRefExpr(RefExpr expr, Object arg) {
		expr.ref.visit(this, FetchType.VALUE);
		return null;
	}

	@Override
	public Void visitCallExpr(CallExpr expr, Object arg) {
		// Get the arguments on the stack
		for (Expression exp : expr.argList)
			exp.visit(this, null);

		if (expr.functionRef instanceof MemberRef) {
			// This is a call to a method of the current class, so we need to
			// place the value of OB before CALL instruction is generated by the
			// MemberRef
			Machine.emit(Op.LOADA, Reg.OB, 0);
		}
		expr.functionRef.visit(this, FetchType.METHOD);
		return null;
	}

	@Override
	public Void visitLiteralExpr(LiteralExpr expr, Object arg) {
		expr.literal.visit(this, null);
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral num, Object arg) {
		Machine.emit(Op.LOADL, Integer.parseInt(num.spelling));
		return null;
	}

	@Override
	public Void visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		Machine.emit(Op.LOADL, bool.spelling.equals("true") ? 1 : 0);
		return null;
	}

	@Override
	public Void visitStringLiteral(StringLiteral str, Object arg) {
		Machine.emit(Op.LOADL, str.spelling.length());
		Machine.emit(Prim.newarr);
		for (int i = 0; i < str.spelling.length(); i++) {
			Machine.emit(Op.LOAD, Reg.ST, -1); // Copy array base address
			Machine.emit(Op.LOADL, i);
			Machine.emit(Op.LOADL, str.spelling.charAt(i));
			Machine.emit(Prim.arrayupd);
		}
		return null;
	}

	@Override
	public Void visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		ClassDecl classDecl = expr.classtype.declaration;

		Machine.emit(Op.LOADL, classDecl.runtimeEntity.classObjectDisplacement);
		Machine.emit(Op.LOADL, classDecl.runtimeEntity.size);
		Machine.emit(Prim.newobj);

		/* Call the initializer function */
		Machine.emit(Op.LOAD, Reg.ST, -1); // Load object's address

		int fieldInitCallAddr = Machine.nextInstrAddr();
		Machine.emit(Op.CALL, Reg.CB, 0);
		methodDisplacements.put(fieldInitCallAddr, classDecl.fieldInitializerEntity);

		/* Call the matching constructor */
		for (Expression e : expr.argList)
			e.visit(this, null);

		Machine.emit(Op.LOAD, Reg.ST, -(1 + expr.argList.size()));

		int callInstrAddr = Machine.nextInstrAddr();
		Machine.emit(Op.CALL, Reg.CB, 0);
		methodDisplacements.put(callInstrAddr, expr.matchedConstructor.runtimeEntity);

		return null;
	}

	@Override
	public Void visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		expr.sizeExpr.visit(this, null);
		Machine.emit(Prim.newarr);
		return null;
	}

	private int getLocalRefDisplacement(LocalRef ref) {
		Declaration decl = ref.identifier.declaration;
		if (decl instanceof VarDecl) {
			return ((VarDecl) decl).runtimeEntity.displacement;
		} else if (decl instanceof ParameterDecl) {
			return ((ParameterDecl) decl).runtimeEntity.displacement;
		} else {
			throw new RuntimeException("LocalRef is not a variable or parameter!");
		}
	}

	@Override
	public Void visitLocalRef(LocalRef ref, Object arg) {
		// We will only visit a LocalRef to get its value
		// Updating values is done in the AssignStmt itself

		if ((FetchType) arg != FetchType.VALUE)
			throw new RuntimeException("visitLocalRef should only be called for VALUE");

		Machine.emit(Op.LOAD, Reg.LB, getLocalRefDisplacement(ref));

		return null;
	}

	@Override
	public Void visitThisRef(ThisRef ref, Object arg) {
		// 'this' should always be used to get the address of current instance
		Machine.emit(Op.LOADA, Reg.OB, 0);
		return null;
	}

	@Override
	public Void visitClassRef(ClassRef ref, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitMemberRef(MemberRef ref, Object arg) {
		Declaration decl = ref.identifier.declaration;

		if (decl instanceof FieldDecl) {
			// Note that a MemberRef for fields is only visited directly if it
			// is the first reference in the qualified reference list. For e.g.
			// if a is a field of the current class, a.b.c will cause a visit to
			// the MemberRef for "a". Everything else will be handled by a
			// DeRef.
			//
			// So, we can assume that we are generating code for accessing a
			// member of the current instance
			int displacement = ((FieldDecl) decl).runtimeEntity.displacement;
			Machine.emit(Op.LOADA, Reg.OB, 0);
			Machine.emit(Op.LOADL, displacement);

			if (((FetchType) arg) == FetchType.VALUE) {
				Machine.emit(Prim.fieldref);
			}

		} else if (decl instanceof MethodDecl) {
			if ((FetchType) arg != FetchType.METHOD)
				throw new RuntimeException("MethodRef called for MethodDecl with unknown arg: " + arg);

			// The MemberRef for methods will be called by a CallExpr. If it is
			// a call to a method in the current class, we will place the value
			// of OB on the stack. Otherwise, DeRef will be used to place the
			// right value of the instance address on the stack. So we assume
			// that the right value is already on the stack.

			MethodDecl methodDecl = (MethodDecl) decl;

			// Do an indirect call only if the method can be overridden
			if (methodDecl.runtimeEntity.isOverridden) {
				Machine.emit(Op.CALLD, methodDecl.parentClass.classObject.indexOf(methodDecl));
			} else {
				int callInstrAddr = Machine.nextInstrAddr();
				Machine.emit(Op.CALL, Reg.CB, 0);
				methodDisplacements.put(callInstrAddr, methodDecl.runtimeEntity);
			}
		}

		return null;
	}

	@Override
	public Void visitDeRef(DeRef ref, Object arg) {
		ref.classReference.visit(this, FetchType.VALUE);

		// Special case for array.length
		Declaration memberDecl = ref.memberReference.getDeclaration();
		if (memberDecl == ArrayType.LENGTH_DECL) {
			Machine.emit(Prim.pred);
			Machine.emit(Op.LOADI);
			return null;
		}

		switch ((FetchType) arg) {
		case ADDRESS:
			Machine.emit(Op.LOADL, ((FieldDecl) ref.memberReference.identifier.declaration).runtimeEntity.displacement);
			break;

		case VALUE:
			Machine.emit(Op.LOADL, ((FieldDecl) ref.memberReference.identifier.declaration).runtimeEntity.displacement);
			Machine.emit(Prim.fieldref);
			break;

		case METHOD:
			ref.memberReference.visit(this, FetchType.METHOD);
			break;
		}

		return null;
	}

	@Override
	public Void visitIndexedRef(IndexedRef ref, Object arg) {
		switch ((FetchType) arg) {
		case ADDRESS:
			// The arrayupd primitive is called by the visitAssignStmt, we just
			// place the arguments on the stack
			ref.ref.visit(this, FetchType.VALUE);
			ref.indexExpr.visit(this, null);
			break;

		case VALUE:
			ref.ref.visit(this, FetchType.VALUE);
			ref.indexExpr.visit(this, null);
			Machine.emit(Prim.arrayref);
		}

		return null;
	}

	/*******************************************
	 * Here lie the unvisited. Do not disturb! *
	 ******************************************/

	@Override
	public Void visitQualifiedRef(QualifiedRef ref, Object arg) {
		throw new RuntimeException("QualifiedRef should not be visited in code generator!");
		// return null;
	}

	@Override
	public Void visitBaseType(BaseType type, Object arg) {
		return null;
	}

	@Override
	public Void visitClassType(ClassType type, Object arg) {
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType type, Object arg) {
		return null;
	}

	@Override
	public Void visitStatementType(StatementType type, Object arg) {
		return null;
	}

	@Override
	public Void visitErrorType(ErrorType type, Object arg) {
		return null;
	}

	@Override
	public Void visitUnsupportedType(UnsupportedType type, Object arg) {
		return null;
	}

	@Override
	public Void visitBadRef(BadRef ref, Object arg) {
		return null;
	}

	@Override
	public Void visitIdentifier(Identifier id, Object arg) {
		return null;
	}

	@Override
	public Void visitOperator(Operator op, Object arg) {
		return null;
	}
}