package miniJava;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.SyntaxErrorException;

public class Compiler {
	private static void printOffendingLine(String fileName, SourcePosition position) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			for(int i = 1; i < position.line; i++)
				in.readLine();
			System.out.println(in.readLine().replace('\t', ' '));
			for(int i = 1; i < position.column; i++)
				System.out.print(' ');
			System.out.println('^');
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
			parser.parseProgram();
		} catch (SyntaxErrorException e) {
			//e.printStackTrace();
			if (e.token != null && e.token.position != null) {
				printOffendingLine(args[0], e.token.position);
			}

			System.out.println(e.getMessage());
			System.exit(4);
		} catch (IOException e) {
			System.out.println("Error opening " + args[0]);
			System.exit(4);
		}
	}
}
