import org.aspectj.testing.Tester;

/** @testcase PR#554 second arg in formal on shared joinpoint with pcd if() causes verify error ?? */
public class PR554 {
  public static void main( String args[] ) {
    String A = makeProduct( "A", new Integer(1) );
    String B = makeProduct( "B", new Integer(0) );
    Tester.check("A".equals(A), "\"A\".equals(A): " + A);
    Tester.check("B".equals(B), "\"B\".equals(B): " + B);
  }
    static String makeProduct(String s, Integer i) { return null; }
}
  
aspect a {
    String around(String whatKind, Integer deleteMeToFixBug): 
        args(whatKind,deleteMeToFixBug) &&
        call(String makeProduct(String,Integer)) &&
        if("A".equals(whatKind)) {
            return "A";
        }
    String around(String whatKind, Integer deleteMeToFixBug): 
        args(whatKind,deleteMeToFixBug) &&
        call(String makeProduct(String,Integer)) &&
        if("B".equals(whatKind)) {
            return "B";
        } 
}


