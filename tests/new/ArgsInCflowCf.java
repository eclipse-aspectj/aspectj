
import org.aspectj.testing.*;

/** PR#660 name binding in around cflow */
public class ArgsInCflowCf {
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
    int around(int x, int y) : // ERR not final
        cflow(call(int foo(int)) && args(x))
        && call(int bar(int)) && args(y) {
        Tester.event(x + "-" + y);
        return proceed(x,y);
    }
    int around(final int x, int y) : 
        cflow(call(int foo(int)) && args(x))
        && call(int bar(int)) && args(y) {
        Tester.event(x + "-" + y);
        return proceed(y,y); // ERR simple refs
    }
    int around(final int x, int y) : 
        cflow(call(int foo(int)) && args(x))
        && call(int bar(int)) && args(y) {
        Tester.event(x + "-" + y);
        return proceed(x+1,y+1); // ERR simple refs
    }
}
