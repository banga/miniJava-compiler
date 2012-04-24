/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.CodeGenerator.ClassRuntimeEntity;
import miniJava.CodeGenerator.MethodRuntimeEntity;
import miniJava.ContextualAnalyzer.Utilities;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassDecl extends Declaration {

	public ClassDecl(Identifier id, ClassType superClass, FieldDeclList fdl, MethodDeclList mdl, OverloadedMethodDecl cdl, SourcePosition posn) {
		super(id, new ClassType(id.spelling, id.posn), posn);
		this.superClass = superClass;
		fieldDeclList = fdl;
		methodDeclList = mdl;
		constructorDecl = cdl;
		((ClassType) type).declaration = this;

		runtimeEntity.size = fdl.size();
		int fieldDisplacement = 0;
		for (FieldDecl fd : fdl) {
			fd.runtimeEntity.displacement = fieldDisplacement++;
		}

		// Provide a default constructor if none has been provided
		if (constructorDecl == null) {
			FieldDecl fd = new FieldDecl(false, false, BaseType.VOID_TYPE, new Identifier(id.spelling, null), null);
			constructorDecl = new OverloadedMethodDecl(fd);
			MethodDecl defaultConstructor = new MethodDecl(fd, new ParameterDeclList(), new StatementList(), null, null);
			constructorDecl.add(defaultConstructor);
		}

		// Set parent class for all methods
		for (OverloadedMethodDecl omd : mdl)
			for (MethodDecl md : omd)
				md.parentClass = this;

		// Set parent class for all constructors
		for (MethodDecl md : constructorDecl)
			md.parentClass = this;
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitClassDecl(this, o);
	}

	/**
	 * Implements member access rules
	 * 
	 * @param md
	 * @param memberID
	 * @param hasPrivateAccess
	 * @param isStaticReference
	 * @return true if the member is accessible
	 */
	private boolean checkAccess(MemberDecl md, Identifier memberID, boolean hasPrivateAccess, boolean isStaticReference) {
		// Private methods cannot be accessed outside class
		if (md.isPrivate && !hasPrivateAccess) {
			Utilities.reportError("Cannot access private member " + md.id + " here", memberID.posn);
			return false;
		}

		// Non-static methods cannot be accessed from static reference
		// Static members can only be referenced through their fully
		// qualified names, starting with class name
		if (!isStaticReference && md.isStatic) {
			Utilities.reportError("Static member " + md.id + " can only be referenced through its parent class name",
					memberID.posn);
			return false;
		}

		// Cannot use the member of a class in "Class.Member" form if it is
		// not static
		if (isStaticReference && !md.isStatic) {
			Utilities.reportError(md.id + " is not a static member of " + super.id, memberID.posn);
			return false;
		}

		return true;
	}

	/**
	 * Return the field with given name if it exists
	 * 
	 * @param fieldName
	 * @return
	 */
	public FieldDecl getFieldDeclaration(Identifier fieldID, boolean hasPrivateAccess, boolean isStaticReference) {
		for (FieldDecl fd : fieldDeclList) {
			if (fd.id.spelling.equals(fieldID.spelling)) {
				if (checkAccess(fd, fieldID, hasPrivateAccess, isStaticReference))
					return fd;
				return null;
			}
		}
		
		if(superClass != null)
			return superClass.declaration.getFieldDeclaration(fieldID, hasPrivateAccess, isStaticReference);
		
		return null;
	}

	/**
	 * Return the method with given name if it exists
	 * 
	 * @param methodName
	 * @return
	 */
	public OverloadedMethodDecl getMethodDeclaration(Identifier methodID, boolean hasPrivateAccess,
			boolean isStaticReference) {
		for (OverloadedMethodDecl md : methodDeclList) {
			if (md.id.spelling.equals(methodID.spelling)) {
				if (checkAccess(md, methodID, hasPrivateAccess, isStaticReference))
					return md;
			}
		}

		if(superClass != null)
			return superClass.declaration.getMethodDeclaration(methodID, hasPrivateAccess, isStaticReference);
		
		return null;
	}

	public MemberDecl getMemberDeclaration(Identifier memberID, boolean hasPrivateAccess, boolean isStaticReference) {
		MemberDecl md = getFieldDeclaration(memberID, hasPrivateAccess, isStaticReference);
		if (md == null)
			md = getMethodDeclaration(memberID, hasPrivateAccess, isStaticReference);
		return md;
	}

	public FieldDeclList fieldDeclList;
	public MethodDeclList methodDeclList;
	public OverloadedMethodDecl constructorDecl;
	public ClassRuntimeEntity runtimeEntity = new ClassRuntimeEntity(0);
	public MethodRuntimeEntity fieldInitializerEntity = new MethodRuntimeEntity(0);

	/* Inheritance */
	public ClassType superClass;
}
