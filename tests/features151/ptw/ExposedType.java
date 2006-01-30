public class ExposedType {
	public static void main(String[] args) {
		new ExposedTypeOne().foo();
		new ExposedTypeTwo().foo();
		new ExposedTypeThree().foo();
	}
}

class ExposedTypeOne {
	public void foo() {	}
}

class ExposedTypeTwo {
	public void foo() {	}
}

class ExposedTypeThree {
	public void foo() {	}
}

aspect X pertypewithin(Exposed*) {
	before(): execution(* foo(..)) {
		System.err.println("here I am "+thisJoinPoint+": for class "+getWithinType());
	}
}