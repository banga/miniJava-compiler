/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Declaration extends AST {
	public Declaration(Identifier id, SourcePosition posn) {
		super(posn);
		this.id = id;
	}
	
	public Identifier id;
}
