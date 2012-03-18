/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class MemberDecl extends Declaration {

	public MemberDecl(boolean isPrivate, boolean isStatic, Type t, Identifier id, SourcePosition posn) {
		super(id, t, posn);
		this.isPrivate = isPrivate;
		this.isStatic = isStatic;
	}

	public MemberDecl(MemberDecl md, SourcePosition posn) {
		super(md.id, md.type, posn);
		this.isPrivate = md.isPrivate;
		this.isStatic = md.isStatic;
	}

	public String toString() {
		return (isPrivate ? "private " : "public ") + (isStatic ? "static " : "") + type.toString() + " " + id.spelling;
	}

	public boolean isPrivate;
	public boolean isStatic;
}
