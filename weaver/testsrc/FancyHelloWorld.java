import java.io.PrintStream;

/**
 * @version 	1.0
 * @author
 */
public abstract class FancyHelloWorld {
    public static void main(String[] args) {
    	PrintStream out = System.out;
    	try {
    		out.println("bye");
    	} catch (Exception e) {
    		out.println(e);
    	} finally {
    		out.println("finally");
    	}
    }
    
    public static String getName() {
    	int x = 0;
    	x += "name".hashCode();
    	return "name" + x;
    }
}
