
class A { void run() {} }
class B extends A {}
aspect C {
    before() : runB() { } 
    pointcut runB(): call(void B.run());  // CW 6 XLint, for each shadow (12, 14) 
    before() : call(int B.run()) {} // pointcut not matched
}
public class DeclaringTypeWarning {
    public static void main(String[] args) {
        // ok with -1.4; otherwise, becomes A.run in bytecode
        new B().run();        // CW 12 DW
        // never works - compile-time type of reference is A, not B
        ((A) new B()).run();
    }
}
aspect D {
    // produces CW 12 DW only under 1.4 (correct method signature)
    declare warning : call(void B.run()) :     // no XLint warning here (?)
        "declare warning : call(void B.run())";
    
    // should never produce a warning
    declare warning : call(int B.run()) :
        "declare warning : call(int B.run())";
}

/** @testcase PR#41952 XLint when call declaring type is not defining type */
