public class PerCflow {
	public static void main(String[] args) {
		m();
	}
	
	static void m() {
		System.out.println("m()");
	}
}


aspect A percflow(pc()) {
	pointcut pc(): execution(void main(..));
	static Object o = new Integer(2);
	before(): execution(void m()) {
		System.out.println("in " + o.toString());
	}
	
	static {
		o = "hello";
	}
}