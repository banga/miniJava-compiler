/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import java.util.Iterator;

import miniJava.CodeGenerator.MethodRuntimeEntity;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class MethodDecl extends MemberDecl {

	public MethodDecl(MemberDecl md, ParameterDeclList pl, StatementList sl, Expression e, SourcePosition posn) {
		super(md, posn);
		parameterDeclList = pl;
		statementList = sl;
		returnExp = e;
	}

	/**
	 * Determines whether method m1 and m2 have same signatures
	 * 
	 * @param m1
	 * @param m2
	 * @return
	 */
	public boolean isEqualTo(MethodDecl md) {
		if (!id.spelling.equals(md.id.spelling) || parameterDeclList.size() != md.parameterDeclList.size())
			return false;

		Iterator<ParameterDecl> it = md.parameterDeclList.iterator();
		for (ParameterDecl pd1 : parameterDeclList) {
			ParameterDecl pd2 = it.next();
			if (!pd1.type.isEqualTo(pd2.type))
				return false;
		}

		return true;
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitMethodDecl(this, o);
	}

	public ClassDecl currentClass;
	public ParameterDeclList parameterDeclList;
	public StatementList statementList;
	public Expression returnExp;
	public MethodRuntimeEntity runtimeEntity = new MethodRuntimeEntity(-1);
}
