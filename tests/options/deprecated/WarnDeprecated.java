



public class WarnDeprecated {

    /** */
    public static void main(String[] args) {
       if (null == args) {
			OldStuff.foo(); // CE 10 deprecated if warn:deprecated
		
			// This is only picked up as a deprecation error when compiling against developer
			// libraries, it will be ignored if compiling against the user jre libraries.
			// We're not going to include this in the test suite for robustness of the suite.
			//"hello".getBytes(0, 1, new byte[10], 0);
       }
    }
}
