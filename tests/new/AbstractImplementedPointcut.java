
/** @testcase PR#36736 implemented abstract pointcut */
public class AbstractImplementedPointcut {
    public static void main(String[] args) {
        new C().go();
    }
}

class C {
    void go(){}
}

abstract aspect A {
    abstract pointcut pc() : call(void go()); // CE 14
}

aspect B extends A {
    pointcut pc() : call(void go());
    before() : pc() {
	throw new Error("do not run");
    }
}
