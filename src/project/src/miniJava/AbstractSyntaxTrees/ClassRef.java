/**
 * miniJava Abstract Syntax Tree classes
 * @author alexisc
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassRef extends SimpleRef {

	public ClassRef(Identifier identifier, SourcePosition posn) {
		super(identifier, posn);
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitClassRef(this, o);
	}

}
