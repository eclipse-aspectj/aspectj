import java.io.*;

public class TryWorksLikeEnvironment {
    static int i;
    public static void main(String[] args) {
	try {
	    foo();
	    try {
		i++;
	    } finally {
		i++;
	    }
	} catch (FileNotFoundException e) {
	}
    }
    static void foo() throws FileNotFoundException {}
}
