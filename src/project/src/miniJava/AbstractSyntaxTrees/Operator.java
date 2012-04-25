/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenType;

public class Operator extends Terminal {

	public TokenType operatorType;

	public Operator(String s, SourcePosition posn) {
		super(s, posn);
		operatorType = (new Token(s, posn)).type;
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitOperator(this, o);
	}

}
