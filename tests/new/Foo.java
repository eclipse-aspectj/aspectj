class Foo {

    public static void main(String[] args) {
	(new Foo()).m1();
	System.out.println("---");
	(new Bar()).m1();
    }
    
    public void m1() { }
}

class Bar extends Foo {
    public void m1()  { super.m1(); }
}

aspect A {
    static before(): instanceof(Foo) && executions(public * *(..)) {
	System.out.println("executions");
    }

 
    static before(): instanceof(Foo) && receptions(public * *(..)) {
	System.out.println("receptions");
    }

}

