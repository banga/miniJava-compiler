/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassType extends Type {
	public ClassType(Identifier id, SourcePosition posn) {
		super(id.spelling, posn);
		typeKind = TypeKind.CLASS;
		name = id;

	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitClassType(this, o);
	}

	public String toString() {
		return name.spelling;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClassType) {
			return spelling.equals(((ClassType) obj).spelling);
		}
		return false;
	}

	public Identifier name;
}
