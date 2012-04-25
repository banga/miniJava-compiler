package tester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Symbol {
	String spelling;

	public Symbol(String spelling) {
		this.spelling = spelling;
	}

	@Override
	public String toString() {
		return spelling;
	}
}

class Terminal extends Symbol {
	public Terminal(String spelling) {
		super(spelling);
	}
}

class NonTerminal extends Symbol {
	String name;
	List<Derivation> rules = new ArrayList<Derivation>();

	public NonTerminal(String spelling) {
		super(spelling);
	}

	void addRule(Derivation d) {
		rules.add(d);
	}

	List<Symbol> substitute() {
		return rules.get((int) (rules.size() * Math.random())).symbols;
	}
}

class Number extends NonTerminal {
	public Number(String spelling) {
		super(spelling);
	}

	int randomDigit() {
		return (int) (Math.random() * 10);
	}

	@Override
	List<Symbol> substitute() {
		String s = randomDigit() + "";

		while (Math.random() > 0.5)
			s = s + randomDigit();

		List<Symbol> symbols = new LinkedList<Symbol>();
		symbols.add(new Terminal(s));

		return symbols;
	}
}

class Identifier extends NonTerminal {
	public Identifier(String spelling) {
		super(spelling);
	}

	int randomDigit() {
		return (int) (Math.random() * 10);
	}

	char randomAlphabet() {
		char c = (char) (65 + Math.random() * 26);
		if (Math.random() > 0.5)
			c += 32;
		return c;
	}

	@Override
	List<Symbol> substitute() {
		String s = randomAlphabet() + "";

		while (Math.random() > 0.5) {
			if (Math.random() > 0.5)
				s = s + randomDigit();
			else
				s = s + randomAlphabet();
		}

		List<Symbol> symbols = new LinkedList<Symbol>();
		symbols.add(new Terminal(s));

		return symbols;
	}
}

class Derivation {
	List<Symbol> symbols = new LinkedList<Symbol>();

	int countNonTerminals() {
		int count = 0;
		for (Symbol s : symbols) {
			if (s instanceof NonTerminal)
				count++;
		}
		return count;
	}

	// Expand a random non-terminal by randomly choosing a substitution
	boolean expandOnce() {
		int count = countNonTerminals();

		if (count == 0)
			return false;

		int i = (int) (Math.random() * count);
		int j = 0;
		for (Symbol s : symbols) {
			if (s instanceof NonTerminal) {
				if (i == 0)
					break;
				i--;
			}
			j++;
		}

		NonTerminal nt = (NonTerminal) symbols.get(j);
		List<Symbol> subst = nt.substitute();
		symbols.remove(j);
		symbols.addAll(j, subst);

		return true;
	}

	@Override
	public String toString() {
		String s = "";
		for (Symbol symbol : symbols) {
			s = s + symbol + " ";
		}
		return s;
	}
}

class Grammar {
	// First symbol is the start symbol
	Map<String, Symbol> symbols = new HashMap<String, Symbol>();
	Symbol startSymbol;

	/**
	 * @param nonTerminals
	 *            a space separated list of non-terminals
	 * @param start
	 *            start non-terminal
	 */
	public Grammar(String nonTerminals, String start) {
		String[] t = nonTerminals.split(" ");

		for (int i = 0; i < t.length; i++)
			symbols.put(t[i], new NonTerminal(t[i]));

		startSymbol = getSymbol(start);
	}

	public void addIdentifierSymbol(String spelling) {
		symbols.put(spelling, new Identifier(spelling));
	}

	public void addNumberSymbol(String spelling) {
		symbols.put(spelling, new Number(spelling));
	}

	private Symbol getSymbol(String spelling) {
		Symbol s = symbols.get(spelling);
		if (s == null)
			throw new RuntimeException("Symbol " + spelling + " not in list of non-terminals");
		return s;
	}

	/**
	 * Add rules for T*
	 * 
	 * @param str
	 *            spelling including the asterisk
	 */
	private void addKleeneStar(String str) {
		if (symbols.containsKey(str))
			return;

		String orig = str.substring(0, str.length() - 1);
		NonTerminal nt = new NonTerminal(str);

		// Derive empty
		nt.rules.add(new Derivation());

		// Derive orig* ::= orig* orig
		Derivation kleene = new Derivation();
		kleene.symbols.add(getSymbol(orig));
		kleene.symbols.add(nt);
		nt.rules.add(kleene);

		symbols.put(str, nt);
	}

	/**
	 * Adds a rule of the form T ::= E a b | X y z | D*
	 * 
	 * @param left
	 * @param right
	 */
	void addRule(String left, String right) {
		String[] rules = right.split("\\|");
		for (int i = 0; i < rules.length; i++)
			if (rules[i].length() > 0)
				addSimpleRule(left, rules[i]);
	}

	private void addSimpleRule(String left, String right) {
		NonTerminal T = (NonTerminal) getSymbol(left);
		String[] rhs = right.split(" ");

		Derivation d = new Derivation();
		for (int i = 0; i < rhs.length; i++) {
			String s = rhs[i];

			if (s.length() == 0)
				continue;

			// Check for Kleene star
			if (s.charAt(s.length() - 1) == '*') {
				addKleeneStar(s);
				d.symbols.add(getSymbol(s));
			} else {
				if (!symbols.containsKey(s))
					symbols.put(s, new Terminal(s));

				d.symbols.add(getSymbol(s));
			}
		}
		T.rules.add(d);
	}

	// Generate random string in the grammar
	String getRandomString(int maxIter) {
		Derivation d = new Derivation();
		d.symbols.add(startSymbol);

		int iter = 0;
		while (d.expandOnce() && iter < maxIter)
			iter++;

		if (d.countNonTerminals() == 0)
			return d.toString();

		return d.toString();
	}
}

public class RandProgGen {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Grammar g = new Grammar(
				"Program ClassDeclaration MemberDeclaration FieldDeclaration MethodDeclaration Statement Declarators Type PrimType ClassType ArrType ParameterList ParameterListTail ArgumentList ArgumentListTail Reference ReferenceTail Expression binop unop",
				"Program");

		g.addIdentifierSymbol("id");
		g.addNumberSymbol("num");
		g.addRule("unop", "- | !");
		g.addRule("binop", "+ | - | / | && | > | < | >= | <= | == | !="); // Can't
																			// include
																			// *
																			// or
																			// ||
																			// at
																			// the
																			// moment

		g.addRule("Program", "ClassDeclaration*");
		g.addRule("ClassDeclaration", "class id {\n MemberDeclaration* }\n");
		g.addRule("MemberDeclaration", "FieldDeclaration | MethodDeclaration");
		g.addRule("MethodDeclaration", "Declarators id () {\n Statement* }\n");
		g.addRule("MethodDeclaration", "Declarators id ( ParameterList ) {\n Statement* }\n");
		g.addRule("MethodDeclaration", "Declarators id () {\n Statement* \n return Expression ;\n }\n");
		g.addRule("MethodDeclaration", "Declarators id ( ParameterList ) {\n Statement*  \n return Expression ;\n }\n");
		g.addRule("FieldDeclaration", "Declarators id ;\n");
		g.addRule("Declarators",
				"Type | static Type | public Type | private Type | public static Type | private static Type");
		g.addRule("Type", "PrimType | ClassType | ArrType");
		g.addRule("PrimType", "int | boolean | void");
		g.addRule("ClassType", "id");
		g.addRule("ArrType", "int [] | ClassType []");
		g.addRule("ParameterList", "Type id ParameterListTail*");
		g.addRule("ParameterListTail", ", Type id");
		g.addRule("ArgumentList", "Expression ArgumentListTail*");
		g.addRule("ArgumentListTail", ", Expression");
		g.addRule("Reference", "this ReferenceTail*");
		g.addRule("Reference", "id ReferenceTail*");
		g.addRule("ReferenceTail", ". id");

		g.addRule("Statement", "{\n Statement* \n}\n");
		g.addRule("Statement", "Type id = Expression ;\n");
		g.addRule("Statement", "Reference = Expression ;\n | Reference [ Expression ] = Expression ;\n");
		g.addRule("Statement", "Reference ( ) ;\n | Reference ( ArgumentList ) ;\n");
		g.addRule("Statement", "if ( Expression ) \n Statement | if ( Expression ) \n Statement else \n Statement");
		g.addRule("Statement", "while ( Expression ) \n Statement");

		g.addRule("Expression", "Reference | Reference [ Expression ]");
		g.addRule("Expression", "Reference ( ) | Reference ( ArgumentList )");
		g.addRule("Expression", "unop Expression");
		g.addRule("Expression", "Expression binop Expression");
		g.addRule("Expression", "( Expression )");
		g.addRule("Expression", "num | true | false");
		g.addRule("Expression", "new id ( ) | new int [ Expression ] | new id [ Expression ]");

		writeTestfiles(g, 100);
	}

	static void writeTestfiles(Grammar g, int nFiles) {
		String filePrefix = "testfiles/rand";

		for (int i = 0; i < nFiles; i++) {
			String str = null;
			while (str == null || str.trim().length() == 0) {
				str = g.getRandomString(10000);
			}

			PrintStream out;
			try {
				out = new PrintStream(new FileOutputStream(filePrefix + i + ".mjava"));
				out.print(str);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < nFiles; i++) {
			String fileName = "../" + filePrefix + i + ".mjava";
			ProcessBuilder pb = new ProcessBuilder("java", "miniJava.Compiler", fileName).directory(new File(System
					.getProperty("java.class.path")));
			pb.redirectErrorStream(true);
			try {
				Process p = pb.start();

				PrintStream outfile = new PrintStream(new FileOutputStream(filePrefix + i + ".out"));
				Scanner scanner = new Scanner(p.getInputStream());
				while (scanner.hasNextLine()) {
					outfile.println(scanner.nextLine());
				}
				outfile.close();

				int exitValue = p.waitFor();
				if (exitValue != 0 && exitValue != 4) {
					System.err.println("Exited with invalid value " + exitValue + " for " + fileName);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
