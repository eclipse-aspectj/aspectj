import org.aspectj.testing.Tester;

public class AdviceOnPrivileged {
    public static void main(String[] args) {
	C c1 = new C();
	c1.getX();

	Tester.check(!c1.gotXOutsideC, "normal field access");

	C c2 = new C();
	ExposeC.getX(c2);

	Tester.check(c2.gotXOutsideC, "privileged field access");
    }
}

class C {
    public boolean gotXOutsideC = false;

    private int x=1;
    public int getX() { return x; }
}

aspect WatchGets {
    before(C c): get(int C.x) && target(c) && !within(C) {
	System.out.println("got C.x");
	c.gotXOutsideC = true;
    }
}

privileged aspect ExposeC {
    public static int getX(C c) { return c.x; }
}
