import org.aspectj.testing.Tester;

public class MethodConflictsCP {
    public static void main(String[] args) {
        C c = new C();
        Tester.checkEqual(c.ma(), "C");        
        Tester.checkEqual(c.m1(), "I1-C");
        Tester.checkEqual(c.m2(), "I2-C");

        I1 i1 = c;
        Tester.checkEqual(i1.m2(), "I2-C");

        Tester.checkEqual(new CO().toString(), "IO");
    }   
}

class C implements I1, I2 {
    public String ma() { return "C"; }
    //private void mp() { }
}

interface BaseI {
    public String m1();
    public String m2();
}

interface I1 extends BaseI {
    static aspect BODY {
        public String I1.m1() { return "I1-" + ma(); }
        public abstract String I1.ma();
    }
}

interface I2 extends BaseI {
    static aspect BODY {
        public String I2.m2() { return "I2-" + ma(); }
        public abstract String I2.ma();
        private String I2.mp() { return "I2"; }
    }
}

interface IO {
    static aspect BODY {
        public String IO.toString() { return "IO"; }
    }
}

class CO implements IO {  
}
