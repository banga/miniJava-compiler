/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class MemberDecl extends Declaration {

    public MemberDecl(boolean isPrivate, boolean isStatic, Type t, Identifier id, SourcePosition posn) {
        super(id, posn);
        this.isPrivate = isPrivate;
        this.isStatic = isStatic;
        this.type = t;
    }
    
    public MemberDecl(MemberDecl md, SourcePosition posn){
    	super(md.id, posn);
    	this.isPrivate = md.isPrivate;
    	this.isStatic = md.isStatic;
    	this.type = md.type;
    }
    
    public boolean isPrivate;
    public boolean isStatic;
    public Type type;
}
