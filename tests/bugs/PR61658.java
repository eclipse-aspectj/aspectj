class A {
	void m() {
	    System.out.println("A");
	}
 }

class B extends A {
        void m() {
	    System.out.println("B");
    }
 }


aspect FunkyPointcut { 

    after(A a, B b) returning:
       call(* foo(*,*)) && 
	(args(b,a) || args(a,b)) { 
        System.out.println("Woven"); 
    } 
}


public class PR61658 { 

    public static void foo(A a, A b) {
	a.m();
        b.m();
    }

    public static void main(String[] args) {
        A a = new A();
        B b = new B(); 
        foo(b,a); 
    } 
 
}