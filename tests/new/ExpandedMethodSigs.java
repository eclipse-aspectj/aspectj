import org.aspectj.testing.Tester;

public class ExpandedMethodSigs {
    public static void main(String[] args) {
	C0 c0 = new C0();
	c0.mI();
	c0.mI1();
	c0.mI2();
	c0.mC();

	I1 i1 = new C0();
	i1.mI();
	i1.mI1();

	C1 c1 = new C1();
	c1.mI();
	c1.mC1();
    }
}

class C0 implements I1, I2 {
    public void mI() { System.out.println("mI"); }
    public void mI1() { System.out.println("mI1"); }
    public void mI2() { System.out.println("mI2"); }

    public void mC() { System.out.println("mC"); }
}

class C1 extends C0 {
    public void mC() { System.out.println("mC from C1"); }
    public void mC1() { System.out.println("mC1"); }
}

interface I1 {
    public void mI();
    public void mI1();
}

interface I2 {
    public void mI();
    public void mI2();
}

aspect A {
    static before(I1 i1): calls(void i1.*()) { System.out.println(">I1.* " + i1); }
    static before(): calls(void I2.*()) { System.out.println(">I2.*"); }
    static before(): calls(void C0.*()) { System.out.println(">C0.*"); }
    static before(): calls(void C1.*()) { System.out.println(">C1.*"); }

    static before(): receptions(void I1.*()) { System.out.println("-I1.*"); }
    static before(): receptions(void I2.*()) { System.out.println("-I2.*"); }
    static before(): receptions(void C0.*()) { System.out.println("-C0.*"); }

    static after() returning(): receptions(I1.new()) { System.out.println("-I1.new"); }
    static after() returning(): receptions(C0.new()) { System.out.println("-C0.new"); }

    static after() returning(): calls(C0, I1.new()) { System.out.println(">I1.new"); }
    static after() returning(): calls(C0, C0.new()) { System.out.println(">C0.new"); }
}
