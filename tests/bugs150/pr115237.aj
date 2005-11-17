public class pr115237 {
	public static void main(String[] args) {
		C c = new C();
		c.go();
		A a = A.aspectOf(c);
		// ok, illegal - aspectOf only on concrete aspects?
		// AA aa = AA.aspectOf(c);

		// hmm - n/a for parameterized types?
		B b = B.aspectOf(c);
		//BB capt  = BB.aspectOf(c); // unexpected compile error here
		//System.out.println("A " + a + " capt " + capt);
	}
	static class C {
		void go() {}		
	}
	
	abstract static aspect AA pertarget(pc()) {
		abstract pointcut pc();
		before() : pc() {
			System.out.println("go()");
		}
	}
	static aspect A extends AA {
		pointcut pc() : call(void C.go());
	}
	
	abstract static aspect BB<T> pertarget(pc()) {
		abstract pointcut pc();
		before() : pc() {
			System.out.println("go()");
		}
	}
	static aspect B extends BB<C> {
		pointcut pc() : call(void C.go());
	}
}
