aspect A {
    public void m(int x) {
        System.out.println(x);
    }

    pointcut foo(String[] args): args(args) && execution(void Hello.*(..));

    before(): execution(* Hello.*(..)) {
        System.out.println("enter");
    }

    public int Hello.fromA;

    public void pingHello(Hello h) {
        int x = 2; 
        System.out.println(x);
    }

}

class Hello {
    public static void main(String[] args) {
    }
}

interface I {
    //void foo();
}
