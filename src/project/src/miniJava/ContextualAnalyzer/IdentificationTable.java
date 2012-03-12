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
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.SyntacticAnalyzer.SyntaxErrorException;

/**
 * A data-structure for managing identifiers in scope. Scoping rules of miniJava
 * are implemented by methods of this class.
 */
public class IdentificationTable {
	public static final int PREDEFINED_SCOPE = 0, CLASS_SCOPE = 1, MEMBER_SCOPE = 2, PARAMETER_SCOPE = 3,
			LOCAL_SCOPE = 4;

	// A stack of tables for each scope
	List<HashMap<String, Declaration>> scopes = new ArrayList<HashMap<String, Declaration>>();

	public IdentificationTable() {
		openScope();

		/*
		 * _PrintStream class and methods
		 */
		MethodDeclList printStreamMethods = new MethodDeclList();
		ParameterDeclList params = new ParameterDeclList();
		params.add(new ParameterDecl(new BaseType(TypeKind.INT, null), new Identifier("n", null), null));

		// public void print(int n);
		MemberDecl print = new FieldDecl(false, false, new BaseType(TypeKind.VOID, null), new Identifier(
				"_PrintStream.print", null), null);
		print = new MethodDecl(print, params, null, null, null);
		printStreamMethods.add((MethodDecl) print);

		// public void println(int n);
		MemberDecl println = new FieldDecl(false, false, new BaseType(TypeKind.VOID, null), new Identifier(
				"_PrintStream.println", null), null);
		println = new MethodDecl(println, params, null, null, null);
		printStreamMethods.add((MethodDecl) println);

		// class _PrintStream
		ClassDecl printStreamDecl = new ClassDecl(new Identifier("_PrintStream", null), new FieldDeclList(),
				printStreamMethods, null);

		/*
		 * System class and member
		 */
		// public static _PrintStream out;
		FieldDeclList systemFields = new FieldDeclList();
		FieldDecl out = new FieldDecl(false, true, new ClassType(new Identifier("_PrintStream", null), null),
				new Identifier("System.out", null), null);
		systemFields.add(out);

		// class System;
		ClassDecl systemDecl = new ClassDecl(new Identifier("System", null), systemFields, new MethodDeclList(), null);

		try {
			set(printStreamDecl);
			set(print);
			set(println);
			set(systemDecl);
			set(out);
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
	 * Sets the declaration of an identifier, if allowed by miniJava rules
	 * 
	 * @param declaration
	 *            <code>Declaration</code> object for the identifier
	 * @throws SyntaxErrorException
	 *             if miniJava does not allow this declaration here
	 */
	public void set(Declaration declaration) throws SyntaxErrorException {
		String name = declaration.id.spelling;
		Declaration decl = null;

		// An identifier in parameter or local scope cannot be re-declared in
		// deeper scopes
		for (int i = PARAMETER_SCOPE; i < scopes.size(); i++) {
			decl = scopes.get(i).get(name);
			if (decl != null)
				throw new SyntaxErrorException(name + " was already declared at " + decl.posn);
		}

		// Re-declaration in current scope not allowed
		HashMap<String, Declaration> scope = scopes.get(scopes.size() - 1);
		decl = scope.get(name);
		if (decl != null)
			throw new SyntaxErrorException(name + " was already declared at " + decl.posn);

		// Add to current scope
		scope.put(declaration.id.spelling, declaration);
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
