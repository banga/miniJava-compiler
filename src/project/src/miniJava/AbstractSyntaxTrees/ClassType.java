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

	/**
	 * Note that this is asymmetric. A.equals(B) returns true if B = A or
	 * derives from A
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClassType) {
			ClassType ct = (ClassType) obj;

			if (declaration == ct.declaration)
				return true;

			if (ct.declaration.superClass != null)
				return this.equals(ct.declaration.superClass);
		}
		return false;
	}

	public ClassDecl declaration = null;
}
