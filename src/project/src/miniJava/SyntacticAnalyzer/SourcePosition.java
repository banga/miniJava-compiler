package miniJava.SyntacticAnalyzer;

public class SourcePosition {

	public int line, column;

	public SourcePosition() {
		line = 1;
		column = 1;
	}

	public SourcePosition(int line, int column) {
		this.line = line;
		this.column = column;
	}
	
	public SourcePosition(SourcePosition position) {
		this.line = position.line;
		this.column = position.column;
	}

	public String toString() {
		return "line " + line + ", column " + column;
	}
}
