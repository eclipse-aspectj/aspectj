import java.io.IOException;

public aspect Pr103097 {
    declare soft: IOException: 
    		within(Pr103097) && 
    		!withincode(* *(..)) &&
    		!call(* *(..));

    before() : execution(* main(..)) {
        try {
	    doThrow();
	} catch (IOException e) {
         throw new RuntimeException("IOException not softened as expected");
	} catch(org.aspectj.lang.SoftException ex) {}
    }

    public static void doThrow() throws IOException {
        throw new IOException("test");
    }

    public static void main(String args[]) {
    }
}