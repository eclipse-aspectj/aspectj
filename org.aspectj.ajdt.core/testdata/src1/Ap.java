package src1;

aspect Ap {
    public void m(int x) {
        System.out.println(x);
    }

    pointcut foo(String[] args): args(args) && execution(void Hello.*(..));

    //pointcut ref(): foo12();

    before(): execution(* Hello.*(..)) {
        System.out.println("enter");
    }

//     before(): get(* java.lang.System.out) {
//         System.out.println("exit get");
//     }

    public int Hello.fromA;

    public void pingHello(Hello h) {
        int x = 2; //XXXh.fromA;
        System.out.println(x);
    }

//     after(): abc {
//         System.out.println("enter");
//     }
//     after() throwing (Throwable t): abc {
//         System.out.println("enter");
//     }

//     public int Hello.m() {
//         System.out.println("m()");
//         return 10;
//     }

//     void around(int x): abc {
//         System.out.println("enter");
//     }
}

class Hello {
    public static void main(String[] args) {
    }
}

interface I {
    //void foo();
}
