import org.aspectj.testing.Tester;
public class IntroducedMethodsOnEachInterface {
    public static void main(String[] args) {
        new IntroducedMethodsOnEachInterface().realMain(args);
    }
    public void realMain(String[] args) {
        I i0 = new I() { public int j() { return 3; } };
        J j0 = new J() { public int j() { return 4; } };
        B b0 = new B();
        I ib = new B();
        J jb = new B();

        Tester.checkEqual(i0.j(), 3, "i0");
        Tester.checkEqual(j0.j(), 4, "j0");
        Tester.checkEqual(b0.j(), 2, "b0");
        Tester.checkEqual(ib.j(), 2, "ib");
        Tester.checkEqual(jb.j(), 2, "jb");

        H h0 = new H() { public int j() { return 7; } };
        H ch = new C();
        C c0 = new C();
        
        Tester.checkEqual(h0.j(), 7, "h0");
        Tester.checkEqual(ch.j(), 6, "ch");
        Tester.checkEqual(c0.j(), 6, "c0");
        
    }
}

interface I {}
interface H {}
interface J { public int j(); }
class B implements I {}
class C implements H {}
aspect A {
    
    declare parents: I implements J;
    declare parents: H implements I;
    declare parents: H implements J;
    
    public int I.j() { return 1; }
    public int B.j() { return 2; }
    public int H.j() { return 5; }
    public int C.j() { return 6; }
}

