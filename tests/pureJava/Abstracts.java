import org.aspectj.testing.Tester;

public class Abstracts {
    static StringBuffer log = new StringBuffer();
    public static void main(String[] args) {
        new D().m();
        Tester.checkEqual(log.toString(), "D.m(), A.m(), A.m(), ");
    }

    public void m() {
        log.append("A.m(), ");
    }
}

abstract class C extends Abstracts {
    public abstract void m();
    public void n() {
        super.m();
    }
}

class D extends C {
    public void m() {
        Abstracts.log.append("D.m(), ");
        super.n();
        n();
    }
}
