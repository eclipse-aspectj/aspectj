import org.aspectj.testing.Tester;

public class MultiInheritCP {
    public static void main(String[] args) {
        C c = new C();
        Tester.checkEqual(c.fromRoot(), "Root");
        Tester.checkEqual(c.toString(), "I1");
        Tester.checkEqual(c.fromI2(), "I2");
        Tester.checkEqual(c.fromIRoot0(), "IRoot");
        Tester.checkEqual(c.fromIRoot1(), "I1");
        Tester.checkEqual(c.fromIRoot2(), "I2");
    }
}

abstract class Root {
    public String fromRoot() { return "Root"; }
    public abstract String fromI2();
}

class C extends Root implements I1, I2 {
}

interface IRoot {
}

interface I1 extends IRoot {
    public String fromRoot();
}

interface I2 extends IRoot {
}


aspect A {
    public String IRoot.fromIRoot0() { return "IRoot"; }
    public String IRoot.fromIRoot1() { return "IRoot"; }
    public String IRoot.fromIRoot2() { return "IRoot"; }


    public String I1.toString() { return "I1"; }
    public abstract String I2.toString();
    public String I2.fromI2() { return "I2"; }

    public String I1.fromIRoot1() { return "I1"; }
    public String I2.fromIRoot2() { return "I2"; }
}
