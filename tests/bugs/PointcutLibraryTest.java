/*
 * This test case produces a ClassFormatError under 1.1.0, but
 * the code is not set up to run/test correctly
 * after the bug is fixed.
 */

/** @testcase PR#40876 subtype-qualified pointcut reference */
public class PointcutLibraryTest {
    public static void main(String[] a) {
        new Test().run();
    }
}

class Test {
    public void run(){ prun(); }
    private void prun() {
        System.out.println("Test.prun()");
    }
}

/** private default implementation of library */
class PrivatePointcutLibrary {
    pointcut adviceCflow() : !cflow(adviceexecution());
    pointcut publicCalls() : call(public * *(..))
        && !adviceCflow();
}

/** public interface for library */
class PointcutLibrary extends PrivatePointcutLibrary {
}

// ---- different clients of the library

/** use library by inheriting scope in class */
class CPL extends PointcutLibrary {
    static aspect A {
        before() : publicCalls() {
            System.out.println("CPL: " 
                + thisJoinPointStaticPart);
        }
    }
}

/** client by external reference to CPL */
aspect ExternalClientOfCPL {
    before() : CPL.publicCalls() { // remove this to avoid bug?
        System.out.println("XDP: " 
            + thisJoinPointStaticPart);
    }
}
