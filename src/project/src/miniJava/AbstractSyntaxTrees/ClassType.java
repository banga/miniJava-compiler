/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassType extends Type
{
    public ClassType(Identifier id, SourcePosition posn){
        super(posn);
        typeKind = TypeKind.CLASS;
        name = id;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitClassType(this, o);
    }

    public Identifier name;
}
