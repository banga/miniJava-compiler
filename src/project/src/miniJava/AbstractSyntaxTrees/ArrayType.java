/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ArrayType extends Type {
	public static final ArrayType STRING_ARRAY_TYPE = new ArrayType(UnsupportedType.STRING_TYPE, "String[]", null);

	/**
	 * Changed Type to store spelling of type for type-checking.
	 * 
	 * @param spelling
	 * @param posn
	 */
	public ArrayType(Type eltType, String spelling, SourcePosition posn) {
		super(spelling, posn);
		typeKind = TypeKind.ARRAY;
		this.eltType = eltType;
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitArrayType(this, o);
	}

	public String toString() {
		return eltType.toString() + "[]";
	}

	@Override
	public boolean isEqualTo(Type type) {
		if (type instanceof ArrayType) {
			return eltType.isEqualTo(((ArrayType) type).eltType);
		} else if(type instanceof NullType) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isEquivalentTo(Type type) {
		if (type instanceof ArrayType) {
			return eltType.isEquivalentTo(((ArrayType) type).eltType);
		} else if(type instanceof NullType) {
			return true;
		}

		return false;
	}

	public Type eltType;
	public ClassDecl declaration = ARRAY_DECL;

	public static final ClassDecl ARRAY_DECL = new ClassDecl(new Identifier("Array", null), null, new FieldDeclList(),
			new MethodDeclList(), null, null);
	public static final FieldDecl LENGTH_DECL = new FieldDecl(false, false, BaseType.INT_TYPE, null, null);
	static {
		// Create the length field for arrays:
		LENGTH_DECL.id = new Identifier("length", null);
		LENGTH_DECL.id.declaration = LENGTH_DECL;
		LENGTH_DECL.runtimeEntity.displacement = -1;
		ARRAY_DECL.fieldDeclList.add(LENGTH_DECL);
	}
}
