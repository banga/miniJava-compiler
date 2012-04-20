package miniJava.ContextualAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.SyntacticAnalyzer.SyntaxErrorException;

/**
 * A data-structure for managing identifiers in scope. Scoping rules of miniJava
 * are implemented by methods of this class.
 */
public class IdentificationTable {
	public static final int PREDEFINED_SCOPE = 0, CLASS_SCOPE = 1, MEMBER_SCOPE = 2, PARAMETER_SCOPE = 3,
			LOCAL_SCOPE = 4, INVALID_SCOPE = -1;

	public static final String PRINTSTREAM = "_PrintStream";
	public static final String PRINTSTREAM_PRINT = "print";
	public static final String PRINTSTREAM_PRINTLN = "println";

	public static final String SYSTEM = "System";
	public static final String SYSTEM_OUT = "out";

	// Needs to be public for testing main method
	public static final ClassDecl STRING_DECL = new ClassDecl(new Identifier("String", null), new FieldDeclList(),
			new MethodDeclList(), null);

	public static MethodDecl PRINTLN_INT_DECL, PRINTLN_STRING_DECL;

	// A stack of tables for each scope
	List<HashMap<String, Declaration>> scopes = new ArrayList<HashMap<String, Declaration>>();

	public IdentificationTable() {
		openScope();

		/*
		 * _PrintStream class and methods
		 */
		MethodDeclList printStreamMethods = new MethodDeclList();
		ParameterDeclList intParam = new ParameterDeclList();
		intParam.add(new ParameterDecl(BaseType.INT_TYPE, new Identifier("n", null), null));

		// public void println(int n);
		FieldDecl fd = new FieldDecl(false, false, BaseType.VOID_TYPE, new Identifier(PRINTSTREAM_PRINTLN, null), null);
		PRINTLN_INT_DECL = new MethodDecl(fd, intParam, null, null, null);
		printStreamMethods.add(PRINTLN_INT_DECL);

		ParameterDeclList stringParam = new ParameterDeclList();
		stringParam.add(new ParameterDecl(STRING_DECL.type, new Identifier("str", null), null));

		// public void println(String str);
		fd = new FieldDecl(false, false, BaseType.VOID_TYPE, new Identifier(PRINTSTREAM_PRINTLN, null), null);
		PRINTLN_STRING_DECL = new MethodDecl(fd, stringParam, null, null, null);
		printStreamMethods.add(PRINTLN_STRING_DECL);

		// class _PrintStream
		ClassDecl printStreamDecl = new ClassDecl(new Identifier(PRINTSTREAM, null), new FieldDeclList(),
				printStreamMethods, null);

		try {
			set(printStreamDecl);
		} catch (SyntaxErrorException e) {
			// Shouldn't occur
		}

		/*
		 * System class and member
		 */
		// public static _PrintStream out;
		FieldDeclList systemFields = new FieldDeclList();
		FieldDecl out = new FieldDecl(false, true, new ClassType(PRINTSTREAM, null), new Identifier(SYSTEM_OUT, null),
				null);
		systemFields.add(out);

		// class System;
		ClassDecl systemDecl = new ClassDecl(new Identifier(SYSTEM, null), systemFields, new MethodDeclList(), null);

		try {
			set(systemDecl);
			set(STRING_DECL);
		} catch (SyntaxErrorException e) {
			// Shouldn't occur
		}

		openScope();
	}

	/**
	 * Returns the declaration of an identifier
	 * 
	 * @param name
	 *            name of the identifier
	 * @return <code>Declaration</code> of the identifier if in scope,
	 *         <code>null</code> otherwise
	 */
	public Declaration get(String name) {
		Declaration decl = null;
		for (int i = scopes.size() - 1; i >= 0; i--) {
			decl = scopes.get(i).get(name);
			if (decl != null)
				break;
		}
		return decl;
	}

	/**
	 * Returns the scope of an identifier
	 * 
	 * @param name
	 *            name of the identifier
	 * @return <code>scope</code> of the identifier if in scope,
	 */
	public int getScope(String name) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(name))
				return i;
		}
		return INVALID_SCOPE;
	}

	/**
	 * Sets the declaration of an identifier, if allowed by miniJava rules
	 * 
	 * @param declaration
	 *            <code>Declaration</code> object for the identifier
	 * @throws SyntaxErrorException
	 *             if miniJava does not allow this declaration here
	 */
	public void set(Declaration declaration) throws SyntaxErrorException {
		String name = declaration.id.spelling;
		// An identifier in parameter or local scope cannot be re-declared in
		// deeper scopes
		for (int i = PARAMETER_SCOPE; i < scopes.size(); i++) {
			Declaration decl = scopes.get(i).get(name);
			if (decl != null)
				throw new SyntaxErrorException(name + " was already declared at " + decl.posn);
		}

		// Re-declaration in current scope not allowed
		HashMap<String, Declaration> scope = scopes.get(scopes.size() - 1);
		Declaration decl = scope.get(name);
		if (decl != null)
			throw new SyntaxErrorException(name + " was already declared at " + decl.posn);

		// Add to current scope
		scope.put(name, declaration);
	}

	/**
	 * Starts a new scope
	 */
	public void openScope() {
		scopes.add(new HashMap<String, Declaration>());
	}

	/**
	 * Closes the deepest level of scope
	 */
	public void closeScope() {
		if (scopes.size() <= 1)
			throw new RuntimeException("IdentificationTable.closeScope() called too many times!");
		scopes.remove(scopes.size() - 1);
	}

	/**
	 * Link declaration of this identifier and return its scope
	 * 
	 * @param id
	 * @return the scope of this identifier
	 */
	public int linkDeclaration(Identifier id) {
		Declaration decl;
		for (int i = scopes.size() - 1; i >= 0; i--) {
			decl = scopes.get(i).get(id.spelling);
			if (decl != null) {
				id.declaration = decl;
				if (i >= LOCAL_SCOPE) {
					return LOCAL_SCOPE;
				}
				return i;
			}
		}
		return INVALID_SCOPE;
	}

	/**
	 * Returns only class declarations
	 */
	public HashMap<String, Declaration> getClasses() {
		if (scopes.size() - 1 >= IdentificationTable.CLASS_SCOPE)
			return scopes.get(IdentificationTable.CLASS_SCOPE);
		return null;
	}

	/**
	 * Returns elements at the class member scope
	 * 
	 * @return HashMap<String, Declaration>
	 */
	public HashMap<String, Declaration> getClassMembers() {
		if (scopes.size() - 1 >= IdentificationTable.MEMBER_SCOPE)
			return scopes.get(IdentificationTable.MEMBER_SCOPE);
		return null;
	}

	public void display() {
		Iterator<HashMap<String, Declaration>> it = scopes.iterator();
		String padding = "";

		while (it.hasNext()) {
			HashMap<String, Declaration> scope = it.next();

			Iterator<String> its = scope.keySet().iterator();
			while (its.hasNext()) {
				String id = its.next();
				System.out.println(padding + "\"" + id + "\"" + ": " + scope.get(id));
			}

			padding = padding + "  ";
		}
	}
}
