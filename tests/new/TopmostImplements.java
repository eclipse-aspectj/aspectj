
import org.aspectj.testing.Tester;
import java.lang.reflect.Method;

// PR#126
        
public class TopmostImplements {
    public static void main(String[] args) { test(); }
    
    public static void test() {
        BC1 bc1 = new BC1();
        BC2 bc2 = new BC2();
        String m1 = "";
        String m2 = "";
        try { 
            m1 = bc1.getClass().getMethod("m1", null).toString();
            m2 = bc2.getClass().getMethod("m1", null).toString();
        } catch ( NoSuchMethodException nsme ) {
            Tester.check(false, "method not found");
        }
        Tester.checkEqual(m1, "public java.lang.String B.m1()", "from extends, implements");
        Tester.checkEqual(m2, "public java.lang.String B.m1()", "from extends");
      
        Tester.checkEqual(bc1.m1(), "BC1", "from extends, implements");
        Tester.checkEqual(bc2.m1(), "BC2", "from extends");  
        
    }
}

aspect Introducer {    
    public String A.m1() { return getName(); }
}


interface A {
    String getName();
}

class B implements A {
    public String getName() { return "B"; }
}

class BC1 extends B implements A {
    public String getName() { return "BC1"; }
} 

class BC2 extends B {
    public String getName() { return "BC2"; }
} 
