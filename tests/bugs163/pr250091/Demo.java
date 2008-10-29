import org.aspectj.lang.reflect.MethodSignature;

class A<X> { }

class Base {
	public A<String> foo() { return null; }
}

public aspect Demo {
	public A<String> Base.bar() { return null; }
	public Base Base.baz() { return null; }
		
	before(): execution(* Base.*(..)) {
		Class<?> cs = ((MethodSignature)thisJoinPointStaticPart.getSignature()).getReturnType();
		System.out.format("%s (%b)%n",
				cs,
				ClassNotFoundException.class == cs);
	}

	public static void main(String[] arg) {
		new Base().foo();
		new Base().bar();
		new Base().baz();
	}
}

