package miniJava;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import mJAM.Disassembler;
import mJAM.Interpreter;
import mJAM.ObjectFile;
import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ASTDisplay;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.CodeGenerator.ASTGenerateCode;
import miniJava.ContextualAnalyzer.ASTIdentifyMembers;
import miniJava.ContextualAnalyzer.ASTReplaceReference;
import miniJava.ContextualAnalyzer.ASTTypeCheck;
import miniJava.ContextualAnalyzer.IdentificationTable;
import miniJava.ContextualAnalyzer.Utilities;
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

	public static void generateAndExecute(AST ast, MethodDecl mainMethod, String fileName) {
		String prefix = fileName.substring(0, fileName.lastIndexOf('.'));

		ASTGenerateCode generatecode = new ASTGenerateCode();
		generatecode.visitPackage((miniJava.AbstractSyntaxTrees.Package) ast, mainMethod);

		/* write code as an object file */
		String objectCodeFileName = prefix + ".mJAM";
		ObjectFile objF = new ObjectFile(objectCodeFileName);
		System.out.print("Writing object code file " + objectCodeFileName + " ... ");
		if (objF.write()) {
			System.out.println("FAILED!");
			return;
		} else {
			System.out.println("SUCCEEDED");
		}

		/* create asm file using disassembler */
		System.out.print("Writing assembly file ... ");
		Disassembler d = new Disassembler(objectCodeFileName);
		if (d.disassemble()) {
			System.out.println("FAILED!");
			return;
		} else {
			System.out.println("SUCCEEDED");
		}

		/* run code */
		System.out.println("Running code ... ");
		Interpreter.interpret(objectCodeFileName);

		System.out.println("*** mJAM execution completed");
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java miniJava.Compiler <filename>");
			System.exit(4);
		}

		try {
			String fileName = args[0];

			Parser parser = new Parser(new FileInputStream(fileName));
			AST ast = parser.parseProgram();

			/* Identification */
			ASTIdentifyMembers identify = new ASTIdentifyMembers();
			IdentificationTable table = identify.createIdentificationTable(ast);

			/* AST modification for QualifiedRefs */
			ASTReplaceReference replace = new ASTReplaceReference();
			replace.visitPackage((miniJava.AbstractSyntaxTrees.Package) ast, table);

			/* Type checking */
			ASTTypeCheck typecheck = new ASTTypeCheck(table);
			MethodDecl mainMethod = typecheck.typeCheck((miniJava.AbstractSyntaxTrees.Package) ast);

			Utilities.exitOnError();

			generateAndExecute(ast, mainMethod, fileName);

			// table.display();
			ASTDisplay display = new ASTDisplay();
			display.showTree(ast);

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
