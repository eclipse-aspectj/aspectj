package p1;

import org.aspectj.testing.Tester;

public class Main3 {
    public static void main(String[] args) {
        Tester.checkEqual(new p1.Foo().getClass().getName(), "p1$Foo");
        //Tester.checkEqual(new p1.p2.Foo().getClass().getName(), "p1.p2.Foo");
    }
}
