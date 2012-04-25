/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassType extends Type {
	public ClassType(String spelling, SourcePosition posn) {
		super(spelling, posn);
		typeKind = TypeKind.CLASS;
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitClassType(this, o);
	}

	public String toString() {
		return spelling;
	}

	@Override
	public boolean isEqualTo(Type type) {
		if (type instanceof ClassType) {
			ClassType ct = (ClassType) type;

			return declaration == ct.declaration;
		}

		return false;
	}

	@Override
	public boolean isEquivalentTo(Type type) {
		if (type instanceof ClassType) {
			ClassType ct = (ClassType) type;

			if (declaration == ct.declaration)
				return true;

			if (ct.declaration.superClass != null)
				return isEquivalentTo(ct.declaration.superClass);
		}

		return false;
	}

	public ClassDecl declaration = null;
}
