
import org.aspectj.testing.Tester;

public class InitializationOrder {
    public static void main (String[] args) {
        C c = new C();   
        Tester.check(null != c.s, "null == c.s");
        Sub s = new Sub();   
        Tester.check("ok" == s.o, "\"ok\" == s.o");
        Tester.check(null == s.p, "null == s.p");
    } 
    
}

class C {
    public String s = null;
    C(String s) { this.s = s; }
    C() { this("uh oh"); }
}

class S { 
    public Object p;
    S(Object p) {this.p = p;}
}

class Sub extends S {
    Sub() {
        super(null); // if (o), then C E illegal use of uninitialized value
        o = "ok";
    }
    Object o;
}

