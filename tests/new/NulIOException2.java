
import org.aspectj.testing.Tester;

class Super {
    Super(Object o){}
}
/** windows treats filename "nul" specially, like /dev/null on unix */
public class NulIOException2 extends Super {
    public static void main(String[] args) {
        Object nul = new Object(); // @testcase nul as identifier
        Object p = new NulIOException2(nul);
        Tester.check(null != nul, "null nul");
        Tester.check(null != p, "null NulIOException2");
    }
    NulIOException2(Object o) { super(o); } 
}
