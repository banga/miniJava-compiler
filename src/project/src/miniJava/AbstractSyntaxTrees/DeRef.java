/**
 * miniJava Abstract Syntax Tree classes
 * @author alexisc
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class DeRef extends Reference {

	public MemberRef memberReference;
	public Reference classReference;// This can either be a DeRef or ClassRef

	public DeRef(Reference classReference, MemberRef memberReference, SourcePosition posn) {
		super(posn);
		this.classReference = classReference;
		this.memberReference = memberReference;

	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitDeRef(this, o);
	}

	@Override
	public String toString() {
		return classReference.toString() + "." + memberReference.toString();
	}

	@Override
	public Declaration getDeclaration() {
		return memberReference.getDeclaration();
	}
}
