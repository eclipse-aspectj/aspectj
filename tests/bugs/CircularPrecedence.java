import org.aspectj.testing.Tester;

/** @testcase Bugzilla Bug 29689  
   Declare precedence should not allow multiple * patterns 

 */
public class CircularPrecedence {

    public static void main(String[] args) {
    }
    
    
	public static aspect Coordinator {
	    declare precedence : *, Tracing, *;  // should be error
	}
}

aspect Tracing {
}
