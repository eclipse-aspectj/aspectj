import java.io.IOException;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class UpdateParserFiles {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			printUsage();
			return;
		}
		Parser.buildFilesFromLPG(args[0]);
	}
	
	public static void printUsage() {
		System.out.println("Usage: UpdateParserFiles <path to javadcl.java>");
		System.out.println("e.g. UpdateParserFiles c:/javadcl.java");
	}
}
