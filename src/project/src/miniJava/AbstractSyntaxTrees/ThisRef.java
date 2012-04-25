/**
 * miniJava Abstract Syntax Tree classes
 * @author alexisc
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ThisRef extends SimpleRef {

	public ThisRef(Identifier identifier, SourcePosition posn) {
		super(identifier, posn);
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitThisRef(this, o);
	}

	@Override
	public String toString() {
		return "this";
	}
}
