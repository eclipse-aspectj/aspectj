public class If {
    public static void main(String[] args) {
        C c = new C();
        c.m(true);
        c.m(false);
        c.m(true);
        c.m(false);
    }
}

class C {
    static boolean test() { return value; }

    static boolean value = false;

    void m(boolean b) {
        value = b;
        System.out.println("C.m(" + b + ")");
    }
}

aspect A {
    static boolean testA() { return true; }
    boolean itestA() { return true; }

    boolean t = true;

    before(): call(void m(boolean)) && if(C.test()) {
        System.out.println(thisJoinPoint);
    }
    before(boolean x): call(void m(boolean)) && args(x) && if(x) && if(testA()) && if(this.t) &&
                               if(thisJoinPoint.getSignature().getName().equals("m")) && if(itestA()) {
        System.out.println(x + ": " + thisJoinPoint);
    }

    pointcut cut(boolean a): call(void m(boolean)) && args(a) && if(a) && if(this.itestA()) && if(t);

    before(boolean x): cut(x) {
        System.out.println(x);
    }

    before(Object t): target(t) && call(void m(boolean)) && if(t instanceof C) {
        System.out.println(t);
    }
    before(Object t): target(t) && call(void m(boolean)) && if(t instanceof String) {
        System.out.println(t);
    }

    before(C c): target(c) && call(void m(boolean)) && if(c.value) {
        System.out.println(thisJoinPoint);
    }
}
