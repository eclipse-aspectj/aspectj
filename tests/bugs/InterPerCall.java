public class InterPerCall {
    public static void main(String[] args) {
        new D().m();
    }
}

class C {
}

class D extends C {
    public void m() { }
}

aspect A perthis(p1()) {
    pointcut p1(): execution(void D.m());
    pointcut p2(): execution(void C.m());
    
    before(): p2() {
        System.out.println("hello");
    }
}