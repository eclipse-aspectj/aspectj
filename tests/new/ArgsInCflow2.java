
import org.aspectj.testing.*;

/** PR#660 name binding in around cflow */
public class ArgsInCflow2 {
    public static void main(String[] args) {
        Tester.check(3==foo(1), "3==foo(1)");
        Tester.checkAllEvents();
    }
    static int foo(int x) {return bar(x+1);}
    static int bar(int x) {return x+1;}

    static { 
        Tester.expectEvent("1-2");
    }
}

aspect Test {
    int around(final int x, final int y) : 
        cflow(call(int foo(int)) && args(x))
        && call(int bar(int)) && args(y) {
        Tester.event(x + "-" + y);
        return proceed(x,y);
    }
}
