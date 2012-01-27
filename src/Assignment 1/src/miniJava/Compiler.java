package miniJava;

import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.ScannerException;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenType;

public class Compiler {
  public static void main(String[] args) {
	  Scanner scanner = new Scanner(System.in);
	  try {
		  while(true) {
			  Token token = scanner.nextToken();
			  System.out.println(token.type);

			  if(token.type == TokenType.EOT)
				  break;
		  }
	  } catch(ScannerException e) {
		  System.out.println(e);
	  }
  }
}
