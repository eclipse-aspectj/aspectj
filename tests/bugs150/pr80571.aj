public class pr80571 {
	
	public static void main(String[] args) {
		new pr80571();
	}
	
}

interface I {
	public final String NAME = "I";
	public pr80571 testObj = new pr80571();
	
}

aspect A {
	Object around() : call(*.new(..)) {
		System.out.println("before");
		Object ret = proceed();
		System.out.println("after");
		return ret;
	}
}