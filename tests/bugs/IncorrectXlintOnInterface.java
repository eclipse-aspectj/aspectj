interface ILib { void run(); }

class UnmatchedCallSupertype implements ILib {
	public static void main(String[] args) {
		new UnmatchedCallSupertype().run();
	}
	public void run() {
		  System.err.println(this.toString());
		  System.err.println(this.toString());
		  System.err.println(this.toString());
		  System.err.println(this.toString());
		}
}

aspect X {

	pointcut runCall() : call(* ILib.*(..));
	pointcut monitor() : call(String clone(String)) || runCall();

	before() : monitor() {
		System.err.println(thisJoinPointStaticPart.getSignature().getDeclaringType().getName());
	}
}

class Client {
	public static void main(String[] args) { new Lib().run(); }
	static class Lib implements ILib { public void run() {} }
}

