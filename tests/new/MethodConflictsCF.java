import org.aspectj.testing.Tester;

public class MethodConflictsCF {
    public static void main(String[] args) {
    }   
}

class C implements I1, I2 {   //ERR: I1.m1() != I2.m1()
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
        public String I2.m1() { return "I2-" + ma(); } //ERR: I1.m1()
        public abstract String I2.ma();
    }
}

