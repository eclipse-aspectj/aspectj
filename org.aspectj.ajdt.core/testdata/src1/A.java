public aspect A issingleton() {
    public void m(int x) {
        System.out.println(x);
    }

    pointcut foo(String[] args): I.foo(args) && execution(void Hello.*(..));

	///XXX add a test for this correctly
	//XXXpublic int Hello.fromA;

    before(): execution(* Hello.*(..)) {
        System.out.println("enter");
    }
    

    public void pingHello(Hello h) {
        int x = 2; 
        System.out.println(x);
    }
    
    public static void main(String[] args) {
    	Hello.main(args);
    }
}

class Hello {
    public static void main(String[] args) {
    }
}


interface I {
	pointcut foo(String[] i): args(i);
	
	static aspect InnerA {
		before(): this(String) {
			System.out.println("hi");
		}
	}
    //void foo();
}
