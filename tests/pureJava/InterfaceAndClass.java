package p;

import org.aspectj.testing.Tester;

public class InterfaceAndClass {
    public static void main(String[] args) {
        Tester.checkEqual(I.type, I.class, "same class");
    }
}

interface I {
    public final Class type = I.class;
}
