public class ParentsFail {
	public static void main(String[] args) {
		I i = new C1();    // CE incompatible
		i.m();
	}
}

class C1 {
	public void m() { System.out.println("m"); }
}
class C2 {}
class C3 extends C2 {}
interface I {
	void m();
}

aspect A {
	declare parents: C2 implements I;  // CE can't implement
	declare parents: C2 extends C3;  // CE circular
	
	declare parents: C1 extends C1; // not considered a CE, just does nothing
}