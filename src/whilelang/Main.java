
package whilelang;

import java.io.File;

import jx86.io.AsmFileWriter;
import jx86.lang.Target;
import jx86.lang.X86File;
import whilelang.ast.WhileFile;
import whilelang.compiler.TypeChecker;
import whilelang.compiler.WhileCompiler;
import whilelang.compiler.X86FileWriter;
import whilelang.testing.X86ValidTests;
import whilelang.util.*;

public class Main {

	/**
	 * A simple entry point for running the compiler. This parses command-line
	 * options and then compiles and executes any while source files supplied.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		boolean verbose = false;
		int fileArgsBegin = 0;

		for (int i = 0; i != args.length; ++i) {
			if (args[i].startsWith("-")) {
				String arg = args[i];
				if (arg.equals("-help")) {
					usage();
					System.exit(0);
				} else if (arg.equals("-version")) {
					System.out.println("While Language Compiler (wlc)");
					System.exit(0);
				} else if (arg.equals("-verbose")) {
					verbose = true;
				} else {
					throw new RuntimeException("Unknown option: " + args[i]);
				}

				fileArgsBegin = i + 1;
			}
		}

		for (int i = fileArgsBegin; i != args.length; ++i) {
			String filename = args[i];
			if(!compileAndExecute(filename,verbose)) {
				System.exit(-1);
			}
		}
	}
	
	/**
	 * Compile and execute a given while source file. This will return true if
	 * the program compiled and executed correctly, otherwise will return false.
	 * 
	 * @param filename
	 *            Filename of while source file to be compiled.
	 * @param verbose
	 *            Flag indicating whether or not to print out detailed
	 *            information when an error occurs.
	 * @return
	 */
	public static boolean compileAndExecute(String filename, boolean verbose) {
		try {			
			WhileCompiler compiler = new WhileCompiler(filename);

			// First, compile the source file
			WhileFile ast = compiler.compile();
			
			// Second, execute it!
			//new Interpreter().run(ast);
			X86File f = new X86FileWriter(X86ValidTests.TARGET).build(ast);
			new AsmFileWriter(new File(filename.replace(".while", ".s"))).write(f);

		} catch (SyntaxError e) {
			// Catch a syntax error which has occurred during one of the
			// compiler stages, such as parsing or type checking.
			if (e.filename() != null) {
				e.outputSourceError(System.out);
			} else {
				System.err.println("syntax error (" + e.getMessage() + ").");
			}
			if (verbose) {
				e.printStackTrace();
			}
			return false;
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			if (verbose) {
				e.printStackTrace();
			}
			return false;
		}
		// Success
		return true;
	}

	/**
	 * Print out information regarding command-line arguments
	 *
	 */
	public static void usage() {
		String[][] info = { 
				{ "version", "Print version information" },
				{ "verbose", "Print detailed information on what the compiler is doing" } 
				};

		System.out.println("usage: wlc <options> <source-files>");
		System.out.println("Options:");

		// first, work out gap information
		int gap = 0;

		for (String[] p : info) {
			gap = Math.max(gap, p[0].length() + 5);
		}

		// now, print the information
		for (String[] p : info) {
			System.out.print("  -" + p[0]);
			int rest = gap - p[0].length();
			for (int i = 0; i != rest; ++i) {
				System.out.print(" ");
			}
			System.out.println(p[1]);
		}
	}
}
