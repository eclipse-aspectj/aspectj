public class TestSourceLines {  // L1
	
	private int i = 0;          // L3
	
	private static int J = 1;   // L5
	private static int K;       // L6
	
	static {                    // L8
		System.out.println("K = 2");
	}
	
	public TestSourceLines() {  // L12
		i = 3;
	}
	
	public TestSourceLines(int i) { // L16
		this.i = i;
	}
	
	public void foo() {         // L20
		System.out.println(i);
	}
	
	private void bar() { System.out.println(i); }  // L24
	
	protected		// L26
	void
	goo()           // L28
	{
		System.out.println(i);
	}
	
}

class NoStaticInitBlock {  // L35
	
}

aspect CheckLineNumbers {  // L39
	
	declare warning : execution(* TestSourceLines.*(..)) : "method execution";
	declare warning : execution(TestSourceLines.new(..)) : "cons execution";
	declare warning : staticinitialization(*) : "static init";
	declare warning : initialization(*.new(..)) : "just-init";
	declare warning : preinitialization(*.new(..)) : "pre-init";
	
	before() : execution(* TestSourceLines.*(..)) {  // L47
		System.out.println("boo");
	}
	
	declare warning : adviceexecution() : "advice";
}