public class PR353d {
    public static void main(String[] args) {
        new PR353d().go();
    }

    void go() {
        System.out.println("\ni..."); I i = new I() { public void f(PR353d d) {} }; i.f(this);
        System.out.println("\na..."); A a = new A(); a.f(this);
        System.out.println("\nb..."); B b = new B(); b.f(this);
        System.out.println("\nc..."); C c = new C(); c.f(this);
        System.out.println("\nd..."); D d = new D(); d.f(this);
        System.out.println("\ne..."); E e = new E(); e.f(this);        
    }
}

interface I {
    public void f(PR353d d);
}

class A {
    public void f(PR353d d) { System.out.println("A.f"); }
}

class B extends A {}
class C extends A {
    public void f(PR353d d) { System.out.println("C.f"); }
}
class D extends A implements I {}
class E extends A implements I {
    public void f(PR353d d) { System.out.println("E.f"); }
}

aspect Aspect {

    pointcut f(): receptions(* *(PR353d));
    pointcut all(): f();
    pointcut anoB(A a): f() &&  instanceof(a) && !instanceof(B);
    pointcut anoC(A a): f() &&  instanceof(a) && !instanceof(C);
    pointcut anoD(A a): f() &&  instanceof(a) && !instanceof(D);    
    pointcut anoE(A a): f() &&  instanceof(a) && !instanceof(E);
    pointcut noA():     f() && !instanceof(A);
    pointcut noB():     f() && !instanceof(B);
    pointcut noC():     f() && !instanceof(C);    
    pointcut noD():     f() && !instanceof(D);
    pointcut noE():     f() && !instanceof(E);      

    static before(): all() { System.out.println("all: " + thisJoinPoint.className); }
    static before(): noA() { System.out.println("noA: " + thisJoinPoint.className); }
    static before(A a): anoB(a) { System.out.println("anoB: " + thisJoinPoint.className); }
    static before(A a): anoC(a) { System.out.println("anoC: " + thisJoinPoint.className); }
    static before(): noB() { System.out.println("noB: " + thisJoinPoint.className); }
    static before(): noC() { System.out.println("noC: " + thisJoinPoint.className); }
    static before(A a): anoD(a) { System.out.println("anoD: " + thisJoinPoint.className); }
    static before(A a): anoE(a) { System.out.println("anoE: " + thisJoinPoint.className); }
    static before(): noD() { System.out.println("noD: " + thisJoinPoint.className); }
    static before(): noE() { System.out.println("noE: " + thisJoinPoint.className); }    
}
