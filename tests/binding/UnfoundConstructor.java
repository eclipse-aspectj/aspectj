

import org.aspectj.testing.Tester;

/** @testcase no such constructor for proceed argument (error) */
public class UnfoundConstructor {
    public static void main (String[] args) {
        I i = new B();
        String s = i.toString();
    } 
}


interface I { }

class B implements I { }

class Mock implements I {
    Mock(B toMock) { }
}

aspect A {
    Object around(I targ) : 
        target(targ) && target(B) && call(* *(..)) {
        return proceed(new Mock(targ)); // CE 25: no such constructor
    }
}
