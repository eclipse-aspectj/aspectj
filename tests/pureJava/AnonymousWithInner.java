// anonymous inner classes with inner types

import org.aspectj.testing.Tester;

public class AnonymousWithInner {

    public static void main(String[] args) {
        new AnonymousWithInner().foo();
        // we're getting two 'cause we called toString twice
        Tester.checkEvents(new String[] { "x = 37", "x = 37" });
    }

    int x = 37;

    void foo() {
        Object inner = new Object() {
                class Inner {
                    void m() {
                        Tester.event("x = " + x); 
                    }
                    public String toString() {
                        m();
                        return "Inner";
                    }
                }
                Object m2() {
                    return new Inner();
                }
            }.m2();
        inner.toString();

        Tester.checkEqual(inner.toString(), "Inner");
    }
}
                
