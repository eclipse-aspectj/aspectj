import org.aspectj.testing.Tester;

public class MultiInheritCF {
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
    public String toString() { return "Root"; }
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


    public String I1.toString() { return "I1"; }  //ERR: conflicts with Root
    public abstract int I2.toString(); //ERR: conflicts with Root and I1
    String I2.fromI2() { return "I2"; } //ERR: weaker than Root

    public String I1.fromIRoot1() { return "I1"; }
    public String I2.fromIRoot1() { return "I1"; } //ERR: conflicts with I1
}
