package miniJava.AbstractSyntaxTrees;

import miniJava.ContextualAnalyzer.IdentificationTable;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class UnsupportedType extends ClassType {
	public static final UnsupportedType STRING_TYPE = new UnsupportedType(IdentificationTable.STRING_DECL, null);

	public UnsupportedType(ClassDecl declaration, SourcePosition position) {
		super(declaration.id.spelling, position);
		super.declaration = declaration;
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
