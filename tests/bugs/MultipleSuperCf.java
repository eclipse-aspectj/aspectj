import org.aspectj.testing.Tester;

interface B1 { }
interface B2 { }

interface D extends B1, B2 {}

aspect A {
	public int B1.m() {
		return 2;
	}
	
	public int D.m() {
		return super.m();  // CE even though B1.m is the only thing that makes sense
	}
}