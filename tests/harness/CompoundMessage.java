
class A { void run() {} }
class B extends A {}
aspect C {
    before() : runB() { } 
    pointcut runB(): call(void B.run());  // CW 6 XLint, for each shadow (12, 14) 

}
public class CompoundMessage {
    public static void main(String[] args) {
        // ok with -1.4; otherwise, becomes A.run in bytecode
        new B().run();        // CW 12 DW
        // never works - compile-time type of reference is A, not B
        new B().run();        // CW 12 DW
    }
}
