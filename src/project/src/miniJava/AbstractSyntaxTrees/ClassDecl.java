/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassDecl extends Declaration {

	public ClassDecl(Identifier id, FieldDeclList fdl, MethodDeclList mdl, SourcePosition posn) {
		super(id, new ClassType(id, posn), posn);
		fieldDeclList = fdl;
		methodDeclList = mdl;
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitClassDecl(this, o);
	}

	public FieldDeclList fieldDeclList;
	public MethodDeclList methodDeclList;

	/**
	 * Return the field with given name if it exists
	 * 
	 * @param fieldName
	 * @return
	 */
	public FieldDecl getFieldDeclaration(String fieldName) {
		// Convert to <class-name>.<member-name> form
		fieldName = this.id.spelling + "." + fieldName;

		for (FieldDecl fd : fieldDeclList) {
			if (fd.id.spelling.equals(fieldName))
				return fd;
		}
		return null;
	}

	/**
	 * Return the method with given name if it exists
	 * 
	 * @param methodName
	 * @return
	 */
	public MethodDecl getMethodDeclaration(String methodName) {
		// Convert to <class-name>.<member-name> form
		methodName = this.id.spelling + "." + methodName;

		for (MethodDecl md : methodDeclList) {
			if (md.id.spelling.equals(methodName))
				return md;
		}
		return null;
	}
	
	public MemberDecl getMemberDeclaration(String memberName) {
		MemberDecl md = getFieldDeclaration(memberName);
		if(md == null)
			md = getMethodDeclaration(memberName);
		return md;
	}
}
