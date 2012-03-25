/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassType extends Type {
	protected ClassType(Identifier id, SourcePosition posn) {
		super(id.spelling, posn);
		typeKind = TypeKind.CLASS;
		name = id;

	}

	/**
	 * Checks for unsupported types before creating a ClassType
	 * 
	 * @param spelling
	 * @return
	 */
	public static Type fromSpelling(Identifier id) {
		if(id.spelling.equals("String"))
			return new UnsupportedType(id.spelling, id.posn);
		return new ClassType(id, id.posn);
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
