public class FinalITDMOnInterface {
	
	public static void main(String[] args) {
		FinalITDMOnInterface f = new Sub();
		f.m();
	}
	
}

class Sub extends FinalITDMOnInterface {
	
	public void m() {
		System.out.println("in class method");
	}
	
}

aspect A {
	
	interface TestInterface {}
	
	public final void TestInterface.m() {
		System.out.println("in aspect declared method");
	}
	
	declare parents : FinalITDMOnInterface implements TestInterface;
	
}