public class PrivateSuperInnerAccess {
    public static void main(String argv[]) {
        Outer.C c = new Outer().new C();
	c.foo();
    }
}

class Outer {
    class B {
        private int x = 5;
    }
    class C extends B {
        int foo() {
            return super.x+1;
        }
    }
}
