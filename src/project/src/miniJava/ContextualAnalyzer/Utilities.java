package miniJava.ContextualAnalyzer;

import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.ErrorType;
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
	 * @param type1
	 * @param type2
	 * @return
	 */
	public static boolean getTypeEquivalence(Type type1, Type type2) {
		if(type1 instanceof UnsupportedType || type2 instanceof UnsupportedType)
			return false;
		if (type1 instanceof ErrorType || type2 instanceof ErrorType)
			return true;
		return type1.equals(type2);
	}

	/**
	 * Report an error if the specified types are not equivalent
	 * 
	 * @param type1
	 * @param type2
	 */
	public static boolean validateTypeEquivalence(Type type1, Type type2, SourcePosition pos) {
		boolean b = getTypeEquivalence(type1, type2);
		if (!b) {
			if(type1 instanceof UnsupportedType || type2 instanceof UnsupportedType) {
				if(type1 instanceof UnsupportedType) {
					reportError("Unsupported type " + type1.spelling, pos);
				}
				if(type2 instanceof UnsupportedType) {
					reportError("Unsupported type " + type2.spelling, pos);
				}
			} else {
				reportError("Type mismatch: Cannot convert from " + type2 + " to " + type1, pos);
			}
		}
		return b;
	}

	public static void reportError(String msg, SourcePosition pos) {
		errorCount++;
		System.err.println("*** " + msg + " at " + pos);
	}

	public static void exitOnError() {
		if(errorCount > 0) {
			System.err.println(errorCount + (errorCount > 1 ? " errors" : " error"));
			System.exit(4);
		}
	}
}
