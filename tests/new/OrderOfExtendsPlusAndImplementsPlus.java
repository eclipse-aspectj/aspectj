import org.aspectj.testing.Tester;
public class OrderOfExtendsPlusAndImplementsPlus {
    public static void main(String[] args) {
        try {
            new OrderOfExtendsPlusAndImplementsPlus().realMain(args);
        } catch (Throwable t) {
            Tester.throwable(t);
        }
    }
    public void realMain(String[] args) throws Exception {
        Class[] cs = new Class[]{C.class, D.class};
        for (int i = 0; i < cs.length; i++) {
            check(cs[i]);
        }
    }
    private void check(Class c) {
        Tester.checkEq(c.getInterfaces()[0], I.class,
                       c + " doesn't implement " + I.class);
        Tester.checkEq(c.getSuperclass(), S.class,
                       c + " doesn't extend " + S.class);
    }
}

class S {}
interface I {}
class C {}
class D {}

aspect A {
    declare parents: C implements I;
    declare parents: C extends S;
}

aspect B {
    declare parents: D extends S;
    declare parents: D implements I;
}
