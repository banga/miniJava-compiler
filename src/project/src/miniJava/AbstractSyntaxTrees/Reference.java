/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Reference extends AST {
	public Reference(SourcePosition posn) {
		super(posn);
	}

	/**
	 * Return the declaration of the entity being referenced
	 * 
	 * @return
	 */
	public abstract Declaration getDeclaration();
	
	
	/**
	 * Update the declaration of the entity being referenced
	 * (used to resolve overloaded methods)
	 */
	public abstract void setDeclaration(Declaration decl);
}
