/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

abstract public class Type extends AST {

	/**
	 * Changed Type to store spelling of type for type-checking.
	 * 
	 * @param spelling
	 * @param posn
	 */
	public Type(String spelling, SourcePosition posn) {
		super(posn);
		this.spelling = spelling;
	}

	public TypeKind typeKind;
	public String spelling;

	/**
	 * Returns true if given Type exactly matches current Type
	 * 
	 * @param type
	 * @return
	 */
	public abstract boolean isEqualTo(Type type);

	/**
	 * Returns true if given Type is the same Type as current Type or given Type
	 * is descendent of current Type
	 * 
	 * @param type
	 * @return
	 */
	public boolean isEquivalentTo(Type type) {
		return isEqualTo(type);
	}
}
