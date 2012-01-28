package miniJava.SyntacticAnalyzer;

@SuppressWarnings("serial")
public class ScannerException extends Exception {
	public String currentString = null;
	public SourcePosition position = null;
	public String extra = "Unknown character";

	public ScannerException(String currentString, SourcePosition position) {
		this.currentString = currentString;
		this.position = position;
	}

	public ScannerException(String currentString, SourcePosition position, String extra) {
		this.currentString = currentString;
		this.position = position;
		this.extra = extra;
	}
	
	public String toString() {
		return extra + ": " + currentString  + " at " + position;
	}
}
