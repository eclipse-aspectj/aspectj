// pr 45184

public class DeclareErrorNamedPointcutCE {
	public static void main(String[] args) {
		new C().run();
	}
}

class C {
	void run() { } // CW expected here
}

aspect A { pointcut pc(): execution(void run()); }

aspect B {
	pointcut ref() : A.pc();  // bug: A.pc() treated as B.pc()
	declare error : ref() : "ref";  
}