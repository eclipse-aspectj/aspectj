import org.aspectj.testing.Tester;

// PR#211

public class Driver {
    public static String s = "s";
    
    public static void main(String[] args) { test(); }

    public static void test() {
        doIt();
        Tester.checkEqual(s, "s:a:b", "both advice worked"); 
    }
    
    public static String doIt() {
        return s;
    }
}

aspect Outer {
    static int N = 10;

    pointcut staticMeth(): within(Driver) && execution(String doIt());

    static aspect Inner {
        static void foo() {
            int i = N;
        }

        before(): staticMeth() {
            Driver.s += ":b";
        }

        pointcut innerPoints(): within(Driver) && execution(void test());
    }

    before(): Outer.Inner.innerPoints() {
        Driver.s += ":a";
    }
}
