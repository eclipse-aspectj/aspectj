
import org.aspectj.testing.Tester;

/** @testcase PR#884 declare constructor on interface subclasses */
public class DeclareInterfaceConstructor {
    public static void main(String[] args) {
        X x = new Z(1);
        if (1 != x.i) {
            Tester.check(false, "bad constructor initialization");
        }
    }
}

interface X {}

class Z implements X {}

aspect Y {
    public int X.i;
    public X+.new(final int i) {this.i = i;}
}

