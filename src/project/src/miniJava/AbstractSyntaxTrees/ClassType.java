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
	public boolean equals(Object obj) {
		if (obj instanceof ClassType) {
			return declaration == ((ClassType) obj).declaration;
			// return spelling.equals(((ClassType) obj).spelling);
		}
		return false;
	}

	public ClassDecl declaration = null;
}
