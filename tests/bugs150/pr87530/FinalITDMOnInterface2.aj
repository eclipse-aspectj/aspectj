public class FinalITDMOnInterface2 {
	
	public static void main(String[] args) {
		FinalITDMOnInterface2 f = new FinalITDMOnInterface2();
		f.m();
	}
	
	public void m() {
		System.out.println("in class method");
	}
	
}

aspect A {
	
	interface TestInterface {}
	
	public final void TestInterface.m() {
		System.out.println("in aspect declared method");
	}
	
	declare parents : FinalITDMOnInterface2 implements TestInterface;
	
}