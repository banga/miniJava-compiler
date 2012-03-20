/**
 * miniJava Abstract Syntax Tree classes
 * @author alexisc
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class SimpleRef extends Reference {

	public Identifier identifier;

	public SimpleRef(Identifier identifier, SourcePosition posn) {
		super(posn);
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return identifier.spelling;
	}

	@Override
	public Declaration getDeclaration() {
		return identifier.declaration;
	}

}
