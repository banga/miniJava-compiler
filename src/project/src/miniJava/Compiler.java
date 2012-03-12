package miniJava;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ASTDisplay;
import miniJava.ContextualAnalyzer.ASTIdentifyMembers;
import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.SyntaxErrorException;

public class Compiler {
	private static void printOffendingLine(String fileName, SourcePosition position) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			for (int i = 1; i < position.line; i++)
				in.readLine();
			String line = in.readLine();
			System.err.println(line);
			for (int i = 1; i < position.column; i++) {
				if (line.charAt(i - 1) == '\t') {
					System.err.print('\t');
				} else {
					System.err.print(' ');
				}
			}
			System.err.println('^');
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java miniJava.Compiler <filename>");
			System.exit(4);
		}

		try {
			Parser parser = new Parser(new FileInputStream(args[0]));
			AST ast = parser.parseProgram();

			ASTDisplay display = new ASTDisplay();
			display.showTree(ast);

//			ASTIdentifyMembers identify = new ASTIdentifyMembers();
//			identify.createIdentificationTable(ast).display();
//			if(identify.errorCount > 0)
//				System.exit(4);

		} catch (SyntaxErrorException e) {
			// e.printStackTrace();
			if (e.token != null && e.token.position != null) {
				printOffendingLine(args[0], e.token.position);
			}

			System.err.println(e.getMessage());
			System.exit(4);
		} catch (IOException e) {
			System.err.println("Error opening " + args[0]);
			System.exit(4);
		}
	}
}
