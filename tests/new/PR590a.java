import org.aspectj.testing.Tester;

public class PR590a {
    public static void main (String args []) {
        staticMethod ();
        new PR590a().instanceMethod("bar");
    }

    public static String staticMethod () {
        return null;
    }

    public String instanceMethod(String a) {
        return "foo";
    }
} 

aspect A {
    static Object fieldX = Boolean.TRUE;
    //static Object Integer = Boolean.TRUE;  // just to screw with you

    pointcut pc(Object s): call(!static String PR590a.*(..)) && args(s);

    before(): target(Byte) { } //sanity check
    //before(): target(BlurghXXX) { } //sanity check, warning in -Xlint



    after () returning (Object s): pc(s) {} //ERR CE 29

    after () throwing (Object e): pc(e) {} //ERR CE 31

    // before(): target(fieldX) { } //ERR, but not handled yet

    //before(): target(Integer) { } //ERR -- finds field rather than type, but not handled yet
} 

