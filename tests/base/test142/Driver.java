// introductions calling super.

import org.aspectj.testing.Tester;

public class Driver {
    public static void test() {
        C2 c2 = new C2("FooBar");

        Tester.checkEqual(c2.name, "FooBar", "C2's name");
    }

    public static void main(String[] args) { test(); }
}

class C1 {
    public String name = null;

    public C1(String name) {
        this.name = name;
    }
}

class C2 extends C1 {
	C2() { super("dummy"); }
}

aspect A {
    C2.new(String name) {
            super(name);
        }
}

