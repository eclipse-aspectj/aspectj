public class InheritedThrows {

    static aspect A {
        after() throwing(Ex1 a): execution(* *.*(..) throws Ex1) {}
    }

    public static class Ex1 extends Exception {}

    public static class Ex2 extends Exception {}

    public interface MyInterface {
        public void m() throws Ex1, Ex2;
    }

    private static class NestedClass1 implements MyInterface {
        public void m() throws Ex1 {} // MATCHES HERE
    }

    private static class NestedClass2 implements MyInterface {
        public void m() throws Ex2 {}
    }

    private static class NestedClassBoth implements MyInterface {
        public void m() throws Ex1, Ex2 {}  // MATCHES HERE
    }

    private static class NestedClassNeither implements MyInterface {
        public void m() {}
    }
}
