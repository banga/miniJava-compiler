package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class UnsupportedType extends ClassType {
	public static final UnsupportedType STRING_TYPE = new UnsupportedType("String", null);

	public UnsupportedType(String spelling, SourcePosition posn) {
		super(new Identifier(spelling, posn), posn);
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitUnsupportedType(this, o);
	}
	
	@Override
	public String toString() {
		return "(Unsupported) " + spelling;
	}
}
