import java.io.*;
import java.util.*;

/**
 * @version 	1.0
 * @author
 */
public class DynamicHelloWorld implements Serializable {

    public static void main(String[] args) {
    	try {
			new DynamicHelloWorld().doit("hello", Collections.EMPTY_LIST);
		} catch (UnsupportedOperationException t) {
			System.out.println("expected and caught: " + t);
			return;
		}
		throw new RuntimeException("should have caught exception");
    }
    
    String doit(String s, List l) {
    	l.add(s);   // this will throw an exception
    	return l.toString();
    }
}
