/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

/**
 * Changed Type to store spelling of type for type-checking.
 * 
 * @param spelling
 * @param posn
 */
public class BaseType extends Type {
	public static final BaseType BOOLEAN_TYPE = new BaseType(TypeKind.BOOLEAN, "boolean", null);
	public static final BaseType INT_TYPE = new BaseType(TypeKind.INT, "int", null);
	public static final BaseType VOID_TYPE = new BaseType(TypeKind.VOID, "void", null);
	public static final BaseType UNSUPPORTED_TYPE = new BaseType(TypeKind.UNSUPPORTED, "", null);
	public static final BaseType OK_TYPE = new BaseType(TypeKind.OK, "", null);

	public BaseType(TypeKind t, String spelling, SourcePosition posn) {
		super(spelling, posn);
		typeKind = t;
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitBaseType(this, o);
	}

	public String toString() {
		return spelling;
	}

	@Override
	public boolean isEqualTo(Type type) {
		if (type instanceof BaseType) {
			return (typeKind == ((BaseType) type).typeKind);
		}
		return false;
	}
}
