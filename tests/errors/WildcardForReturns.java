
import org.aspectj.testing.Tester;





/** @testcase PR#280 wildcard used for returns clause */
public class WildcardForReturns {
    public static void main(String[] args) { 
        new WildcardForReturns().m(); 
    }
    
    public void m() { }
}
aspect A {
   * around (WildcardForReturns t): this(t) && call(* m()) {  // CE 17
    	// bad test - return null; //return proceed(t);
   }
}
