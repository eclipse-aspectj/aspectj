import org.aspectj.testing.Tester;

public class DefaultConsAndInner {
    public static void main(String[] args) {
        new Outer.Inner("test");
        Tester.check("test");
    }
}

class Outer {
    static class Inner extends Outer {
        public Inner(String s) {
            Tester.note(s);
        }
    }
}
