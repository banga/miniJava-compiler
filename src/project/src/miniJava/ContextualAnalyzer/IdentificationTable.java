package miniJava.ContextualAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import miniJava.AbstractSyntaxTrees.Declaration;
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
		HashMap<String, Declaration> predefined = new HashMap<String, Declaration>();

		// TODO populate the predefined scope
		// Should we add the list of miniJava keywords here?

		scopes.add(predefined);
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

		// An identifier in local scope cannot be re-declared in deeper scopes
		for (int i = LOCAL_SCOPE; i < scopes.size(); i++) {
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

	public void display() {
		Iterator<HashMap<String,Declaration>> it = scopes.iterator();
		String padding = "";

		while(it.hasNext()) {
			HashMap<String, Declaration> scope = it.next();

			Iterator<String> its = scope.keySet().iterator();
			while(its.hasNext()) {
				String id = its.next();
				System.out.println(padding + id + ": " + scope.get(id));
			}
			
			padding = padding + "  ";
		}
	}
}
