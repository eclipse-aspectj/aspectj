import org.aspectj.testing.*;

privileged aspect PrivilegedAspect {
    public void OtherClass.foo() {
	Tester.event("foo.intro");
    }
}

class OtherClass {
}

public class Privileged {
    public static void main(String[] args) {
        new Privileged().go();
    }

    static {
        Tester.expectEventsInString("foo.intro,foo.done,foo.called");
    }

    void go() {
        Tester.event("foo.called");
        new OtherClass().foo();
        Tester.event("foo.done");
    }
}
