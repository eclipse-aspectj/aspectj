import org.aspectj.testing.Tester;

public class PR590 {
    public static void main (String args []) {
        staticMethod ();
        new PR590().instanceMethod("bar");
    }

    public static String staticMethod () {
        return null;
    }

    public String instanceMethod(String a) {
        return "foo";
    }
} 

aspect A {
    after () returning (String s):
        execution(static String PR590.staticMethod()) && if(s == null) { } //ERR

    after () throwing (Error e):
        execution(static String PR590.staticMethod()) && if(e != null) { } //ERR
} 

