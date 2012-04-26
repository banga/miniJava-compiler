package miniJava.ContextualAnalyzer;

import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.Type;
import miniJava.AbstractSyntaxTrees.UnsupportedType;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.SyntaxErrorException;

public abstract class Utilities {
	private static int errorCount = 0;

	/**
	 * Add a declaration to the identification table and check if the declared
	 * type is valid
	 * 
	 * @param table
	 * @param declaration
	 */
	public static void addDeclaration(IdentificationTable table, Declaration declaration) {
		try {
			table.set(declaration);
		} catch (SyntaxErrorException e) {
			reportError(e.getMessage(), declaration.posn);
		}
	}

	/**
	 * Check if the mentioned type has beeen declared in the identification
	 * table
	 * 
	 * @param spelling
	 * @param table
	 */
	public static void validateType(Type type, IdentificationTable table) {
		if (type instanceof ArrayType) {
			type = ((ArrayType) type).eltType;
		}

		if (type instanceof BaseType || type instanceof UnsupportedType) {
			return;
		}

		int scope = table.getScope(type.spelling);
		if (scope > IdentificationTable.CLASS_SCOPE) {
			reportError("The type " + type + " is not a valid class name", type.posn);
		} else if (scope == IdentificationTable.INVALID_SCOPE) {
			reportError("The type " + type + " is undefined", type.posn);
		}
	}

	/**
	 * Return true if the two types are equivalent
	 * 
	 * @param lhsType
	 * @param rhsType
	 * @param isSymmetric
	 *            true if lhs is allowed to be the derived type (used for ==)
	 * @return
	 */
	public static boolean getTypeEquivalence(Type lhsType, Type rhsType, boolean isSymmetric) {
		return lhsType.isEquivalentTo(rhsType) || (isSymmetric && rhsType.isEquivalentTo(lhsType));
	}

	/**
	 * Report an error if the specified types are not equivalent
	 * 
	 * @param lhsType
	 * @param rhsType
	 * @param isSymmetric
	 *            true if lhs is allowed to be the derived type (used for ==)
	 * @param pos
	 */
	public static boolean validateTypeEquivalence(Type lhsType, Type rhsType, boolean isSymmetric, SourcePosition pos) {
		boolean b = getTypeEquivalence(lhsType, rhsType, isSymmetric);
		if (!b) {
			if (lhsType instanceof UnsupportedType || rhsType instanceof UnsupportedType) {
				if (lhsType instanceof UnsupportedType) {
					reportError("Unsupported type " + lhsType.spelling, pos);
				}
				if (rhsType instanceof UnsupportedType) {
					reportError("Unsupported type " + rhsType.spelling, pos);
				}
			} else {
				reportError("Type mismatch: Cannot convert from " + rhsType + " to " + lhsType, pos);
			}
		}
		return b;
	}

	public static void reportError(String msg, SourcePosition pos) {
		errorCount++;
		System.err.println("*** " + msg + " at " + pos);
	}

	public static void exitOnError() {
		if (errorCount > 0) {
			System.err.println(errorCount + (errorCount > 1 ? " errors" : " error"));
			System.exit(4);
		}
	}
}
