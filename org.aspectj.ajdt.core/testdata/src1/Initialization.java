public class Initialization extends Super {
	public static void main(String[] args) { 
		new Initialization();
		new Initialization(331);
	}
	
	Initialization() {
		this(98);
	}
	Initialization(int i) { 
		super(i+1);
		foo(i);
    }
	
	static void foo(int i) {
		System.out.println("running foo with " + i);
	}	
}

class Super {
	Super(int i) {
	}
}

aspect Foo {


//	void around(int i): initialization(Initialization.new(int)) && args(i) {
//		System.out.println(thisJoinPoint);
//		System.out.println(java.util.Arrays.asList(thisJoinPoint.getArgs()));
//		proceed(993);
//	}
//	

	
	Object around(): preinitialization(Initialization.new()) {
		System.out.println("i");
		return null;
	}
	
//	before(int i): execution(Initialization.new(int)) && args(i) {
//		System.err.println(i);
//	}
}


