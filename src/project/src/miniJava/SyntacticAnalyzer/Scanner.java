package miniJava.SyntacticAnalyzer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Converts the input stream into tokens for the miniJava language
 */
public class Scanner {
	private InputStream in;
	private char currentChar;
	private StringBuilder currentString;
	private SourcePosition position;

	final static char EOT = '\u0000';

	public Scanner(InputStream in) {
		this.in = in;
		this.currentChar = ' ';
		this.currentString = new StringBuilder();
		this.position = new SourcePosition(1, 0);
	}

	private void skipLine() throws IOException {
		while (!StringUtils.isNewline(currentChar) && currentChar != EOT)
			nextChar();
	}

	private void skipWhitespace() throws IOException {
		while (StringUtils.isWhiteSpace(currentChar))
			nextChar();
	}

	private void skipMultiLineComment() throws IOException, ScannerException {
		// Multi-line comments
		SourcePosition commentStart = new SourcePosition(position);
		while (true) {
			while (currentChar != '*' && currentChar != EOT)
				nextChar();
			if (currentChar == '*' && nextChar() == '/') {
				nextChar();
				break;
			}
			if (currentChar == EOT)
				throw new ScannerException("/*", commentStart, "Multi-line comments not closed");
		}
	}

	/**
	 * Returns the next token in stream
	 * 
	 * @return Token
	 * @throws ScannerException
	 *             if an unknown column is encountered
	 */
	public Token nextToken() throws ScannerException {
		try {
			skipWhitespace();

			switch (currentChar) {

			// Binary operators
			case ';':
			case ',':
			case '.':
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case '+':
			case '*':
				Token token = new Token(Character.toString(currentChar), position);
				nextChar();
				return token;

			case '-':
				nextChar();
				if(currentChar == '-')
					throw new ScannerException("--", position, "-- is not supported");
				return new Token("-", position);

			case '|':
				nextChar();
				expect('|');
				return new Token("||", position);

			case '&':
				nextChar();
				expect('&');
				return new Token("&&", position);

			case '<':
			case '>':
			case '=':
			case '!':
				String spelling = Character.toString(currentChar);
				nextChar();
				// Account for >=, <=, == and !=
				if(currentChar == '=') {
					spelling = spelling + currentChar;
					nextChar();
				}
				return new Token(spelling, position);

			case '/':
				nextChar();
				if (currentChar == '/') {
					// Single line comment
					skipLine();
					return nextToken();
				} else if (currentChar == '*') {
					nextChar();
					skipMultiLineComment();
					return nextToken();
				}
				return new Token(TokenType.SLASH, "/", position);

			case '"':
				nextChar();
				StringBuilder str = new StringBuilder();
				while(currentChar != '"' && currentChar != EOT) {
					str.append(currentChar);
					nextChar();
				}
				expect('"');
				return new Token(TokenType.STRING, str.toString(), position);

			case EOT:
				return new Token(TokenType.EOT, "", position);

			default:
				currentString.setLength(0);
				currentString.append(currentChar);

				if (StringUtils.isAlpha(currentChar)) {
					// Identifier
					nextChar();
					while (StringUtils.isAlnum(currentChar) || currentChar == '_') {
						currentString.append(currentChar);
						nextChar();
					}
					return new Token(currentString.toString(), position);
				} else if (StringUtils.isDigit(currentChar)) {
					// Number
					nextChar();
					while (StringUtils.isDigit(currentChar)) {
						currentString.append(currentChar);
						nextChar();
					}
					return new Token(TokenType.NUMBER, currentString.toString(), position);
				}

				// Unknown character
				throw new ScannerException(Character.toString(currentChar), position);
			}
		} catch (IOException e) {
			return new Token(TokenType.EOT, "", position);
		}
	}

	/**
	 * Consumes expected character if found
	 * 
	 * @param expectedChar
	 * @throws ScannerException if expected character is not found
	 * @throws IOException
	 */
	private void expect(char expectedChar) throws ScannerException, IOException {
		if(currentChar != expectedChar)
			throw new ScannerException(Character.toString(currentChar), position, "Expected " + expectedChar);
		nextChar();
	}

	/**
	 * Reads the next character from stream
	 * 
	 * @return the next character from input or EOT on end-of-file
	 * @throws IOException
	 */
	private char nextChar() throws IOException {
		int c = in.read();

		if (c == -1) {
			currentChar = EOT;
		} else {
			currentChar = (char) c;
			if (currentChar == '\r') {
				return nextChar();
			}
			if(currentChar == '\n') {
				position.column = 0;
				position.line++;
			} else {
				position.column++;
			}
		}

		return currentChar;
	}
}

/**
 * Utility class for operations on characters and strings
 */
class StringUtils {
	public static boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	public static boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}

	public static boolean isAlnum(char c) {
		return isAlpha(c) || isDigit(c);
	}

	public static boolean isNewline(char c) {
		return (c == '\r' || c == '\n');
	}

	public static boolean isWhiteSpace(char c) {
		return (c == ' ' || c == '\t' || isNewline(c));
	}
}