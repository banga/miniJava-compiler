package miniJava.SyntacticAnalyzer;

@SuppressWarnings("serial")
public class ScannerException extends Exception {
	public SourcePosition position = null;
	public String spelling = null;

	public ScannerException(String spelling, SourcePosition position) {
		super("unrecognized token " + spelling + " at " + position);
		this.spelling = spelling;
		this.position = position;
	}

	public ScannerException(String spelling, SourcePosition position, String extra) {
		super(extra + ": " + spelling + " at " + position);
		this.spelling = spelling;
		this.position = position;
	}
}
