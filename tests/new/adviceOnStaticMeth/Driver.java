
import org.aspectj.testing.Tester;

// PR#221

public class Driver {
    public static String s = "s";
    
    public static void main(String[] args) { test(); }

    public static void test() {
        Tester.checkEqual(doIt(), s, "advice worked"); 
    }
    
    public static String doIt() {
        return s;
    }
}

aspect Outer {
    pointcut staticMeth(): within(Driver) && execution(String doIt());

    /*static*/ before(): staticMeth() {
        Driver.s += ":a";
    }    

}
