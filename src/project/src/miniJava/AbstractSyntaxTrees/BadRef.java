package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

/**
 * Represents a malformed reference in the AST
 */
public class BadRef extends SimpleRef {

	public BadRef(Identifier identifier, SourcePosition posn) {
		super(identifier, posn);
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitBadRef(this, o);
	}

}
