
import org.aspectj.testing.Tester; 

/** @testcase package typepattern with no packages (in default package) */
public class TypeNames {
    public static void main (String[] args) {
        new Suffix().run();
        new MySuffix().run();
        Tester.checkAllEvents();
    } 
    static {
        Tester.expectEvent("Suffix.run()");
        Tester.expectEvent("MySuffix.run()");
    }
}

// classes not to be matched by TypePattern below
class Suffix { 
    void run() {
        Tester.event("Suffix.run()");
    } 
}

class MySuffix { 
    void run() {
          Tester.event("MySuffix.run()");
    } 
}



aspect A {
    // BUG: This is all that's required to provoke the bug in -Xlint mode
    declare parents: *..*Suffix implements Runnable;  // lint: no type matched
 
 

    // coverage cases
    before() : staticinitialization(*..*Suffix) { // lint: no type matched
        Tester.check(false, "no such join point");
    }

    before() : call(void *..*Suffix.run()) { // lint: no type matched
        Tester.check(false, "no such join point");
    }

    before() : call(*..*Suffix.new()) { // lint: no type matched
        Tester.check(false, "no such join point");
    }
}
