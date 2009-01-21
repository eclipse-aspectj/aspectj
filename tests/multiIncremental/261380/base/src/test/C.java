package test;

public class C {
	public C() {
		new C(); 
	}
	
//	public static void main(String[] args) {
//		new C();
//	}
}

aspect X {
    before () : call(public *..C.new()) {}
}
