/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Declaration extends AST {
	public Declaration(Identifier id, Type type, SourcePosition posn) {
		super(posn);
		this.id = id;
		this.type = type;
	}

	public Identifier id;
	public Type type;
}
