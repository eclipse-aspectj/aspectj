import org.aspectj.testing.*;

public class PR347 {
    public static void main(String[] args) {
        new PR347().realMain(args);
    }
    public void realMain(String[] args) {
        new A().i();
        new B().j();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEventsInString("Ai,A.i,Bj,B.j");
    }
}
interface I { public void i(); }
interface J { public void j(); }

class A {}
class B {}

aspect Aspect1 {
    A +implements I;
    B +implements J;
}

aspect Aspect2 {
    pointcut Ai(): receptions(void i()) && instanceof(A);
    pointcut Bj(): receptions(void j()) && instanceof(B);
    before(): Ai() { Tester.event("Ai"); }
    before(): Bj() { Tester.event("Bj"); }
}

aspect Aspect3 {
    public void A.i() { Tester.event("A.i"); }
    public void B.j() { Tester.event("B.j"); }
}
