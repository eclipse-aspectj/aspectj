
import org.aspectj.testing.Tester;

/** @testcase Bugzilla Bug 29662  
   VerifyError on accessing objects not accessible to the weaver: Incompatible object argument for invokespecial 
 */
public class AroundAccess {
    public static void main(String args[]) throws Throwable {
        AroundAccess ve = new AroundAccess();
        ve.foo();
        Tester.checkEqual(FinalizeContract.fromAround, "s3:2,ME");
    }

    protected void foo() throws Throwable {}
}

class Foo {
	private static int x;
}


aspect FinalizeContract {
	public static String fromAround;
	
    pointcut finalizeCall(Object o):
        this(Object+) &&
        this(o) &&
        execution(void foo());

    void around(Object o) throws Throwable: finalizeCall(o) {
    	String p = getS(3.14, 2); // + Foo.x;
    	fromAround = p + "," + toString();
    	Tester.checkNotEqual(super.toString(), toString());
        proceed(o);
        counter++;
    }

	private String getS(double d, int i) { return "s" + ((int)d) + ":" + i; }

	public String toString() { return "ME"; }
	
	private long counter = 0;
}