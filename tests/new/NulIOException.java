
import org.aspectj.testing.Tester;

class Super {
    Super(Object o){}
}

/** windows treats filename "nul" specially, like /dev/null on unix */
public class NulIOException extends Super {

    public static void main(String[] args) {
        Object o = new NulIOException(nul); // parms: IOException
        Tester.check(false, "expecting compiler error");
        Object p = nul; // reference: expect CE here, not IOException
    }

    NulIOException() { super(nul); }

    // super parms: expect CE here, not IOException 
    NulIOException(Object o) { super(o); }
    // don't attempt to read nul on windows
}
  
