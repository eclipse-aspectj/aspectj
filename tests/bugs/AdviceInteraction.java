public class AdviceInteraction {
    public static void main(String [] args) {
        new C().m1();
    }
}

class C {    
    public void m1() {}
    public void m2() {}
}

aspect A {    
    pointcut exec1(C c): this(c) && execution(void m1());
    pointcut execs(C c): exec1(c); 
    
    before (): execs(*) {}
    before (C c):  execs(c) {}

    // This ordering works correctly
    pointcut exec2(C c): this(c) && execution(void m2());
    pointcut execs2(C c): exec2(c); 
    
    before (C c):  execs2(c) {}
    before (): execs2(*) {}
}