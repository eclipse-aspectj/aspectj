public class PerObject {
	public static void main(String[] args) {
		new PerObject().m();
	}
	
	void m() {
		System.out.println("m()");
	}
}


aspect A perthis(pc()) {
	pointcut pc(): this(PerObject) && execution(* m(..));
	static Object o = new Integer(2);
	before(): get(* *) {
		System.out.println("in " + o.toString());
	}
	
	static {
		o = "hello";
	}
}