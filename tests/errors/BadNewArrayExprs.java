class BadNewArrayExprs {
    static Object o;
    public static void main(String[] args) {
	o = new Test[][3];
	o = new Test[][];
	o = new Test[];
	o = new Test[3] { };
    }
}
/*

abstract class C { 
}
class D extends C {
}

class M {}

class X {
    abstract void foo();
}

class Y extends X {

    void foo(M m) {
	new C().m(m);
    }

    static aspect XXX {
	abstract private void C.m(M m); 

	private void D.m(M m) {
	    System.out.println("I'm in XXX " + m);
	}
    }
}

class Z extends X {
    void foo(M m) {
	new C().m(m);
    }

    static aspect XXX {
	abstract private void C.m(M m); 

	private void D.m(M m) {
	    System.out.println("I'm in YYY " + m);
	}
    }	
}
*/
