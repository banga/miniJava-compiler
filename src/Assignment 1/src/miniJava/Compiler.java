package miniJava;

import java.io.FileInputStream;
import java.io.IOException;

import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.SyntaxErrorException;

public class Compiler {
  public static void main(String[] args) {
	  if(args.length < 1) {
		  System.out.println("Usage: java miniJava.Compiler <filename>");
		  System.exit(4);
	  }

	  try {
		  Parser parser = new Parser(new FileInputStream(args[0]));
		  parser.parseProgram();
	  } catch(SyntaxErrorException e) {
		  e.printStackTrace();
		  System.out.println(e);
		  System.exit(4);
	  } catch(IOException e) {
		  System.out.println("Error opening " + args[0]);
		  System.exit(4);
	  }
  }
}
