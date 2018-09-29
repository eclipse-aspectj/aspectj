public class Code {

    public static void main(String[] args) {
        A.methodA();
    }

}

class A {

    public static void methodA() {
        B.methodB();
    }

}

class B {

    public static void methodB() {
        C.methodC();
        int a = 1;
        int b = 2;
        System.out.println( a + b );
    }

}

class C {

    public static void methodC() {
        D.methodD();
    }

}

class D {

    public static void methodD() {

    }

}

aspect CFlow {

    public pointcut flow() : cflow(call( * B.methodB() ) ) && !within(CFlow);

    before() : flow() {
        System.out.println( thisJoinPoint );
    }

}

