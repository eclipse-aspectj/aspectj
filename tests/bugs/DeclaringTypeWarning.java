
class A { void run() {} }
class B extends A {}
aspect C {
    before() : runB() { } // warn here
    pointcut runB(): call(void B.run());
    before() : call(int B.run()) {}
}
public class DeclaringTypeWarning {
    public static void main(String[] args) {
        // ok with -1.4; otherwise, becomes A.run in bytecode
        new B().run();        
        // never works - compile-time type of reference is A, not B
        ((A) new B()).run();
    }
}
aspect D {
    declare error : call(void B.run()) : // warn here
        "This should be the only error";
    
    declare error : call(int B.run()) :
        "This error should not happen";
}
