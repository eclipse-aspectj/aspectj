public aspect Failing {
    pointcut failingPointcut() : execution(* foo*(..));

    after() returning() : failingPointcut()
    {
        System.out.println("hit");
    }
}

class X <T extends Object> {
    // Pointcut match highlighted
//    void foo() {}

    // Pointcut match highlighted
//    void foo1(T x) {}   

    // Pointcut not highlighted
    void foo2(T[] x) {}

    // Pointcut not highlighted
//    void foo3(T... x) {}

    // Pointcut highlighted
//    T foo3() { return null; }

    // Pointcut highlighted
//    T[] foo4() { return null; }

    public static void main(String[] args) {
        X<Object> x = new X<Object>();

        x.foo2(null);       
    }
}

