import org.aspectj.testing.Tester;

public class DoubledCalls {
    public static void main(String[] args) {
	new Runnable() {
		public void run() {
		    new Integer(1).intValue();
		}
	    }.run();
	Tester.check(A.calledInteger, "A.calledInteger");
    }
}

aspect A {
    static boolean calledInteger = false;

    /*static*/ before(): call(* Integer.*(..)) {
	A.calledInteger = true;
    }
}
