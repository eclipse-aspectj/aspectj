public class TestingArgsAnnotations {

	private static boolean[] expected;
	private static int index = 0;

	private static void setExpectedMatches(boolean[] matches) {
		System.out.println();
		System.out.println("New Test Run");
		System.out.println("===============================");
		expected = matches;
		index = 0;
	}
	
	public static boolean expected() {
		System.out.print("Test " + index + ": ");
		return expected[index++];
	}
	
	public static void main(String[] args) {
		A a = new A();
		B b = new B();
		C c = new C();
		D d = new D();
		E e = new E();
		
		A reallyB = new B();
		C reallyD = new D();
		D reallyE = new E();
		
		// now make some calls...
		setExpectedMatches(new boolean[] {true,false,false,true,true,false,false,false,true,true,false});
		myMethod(a,b,c,d,e);
		
		setExpectedMatches(new boolean[] {true,false,false,true,true,true,true,false,true,true,false});
		myMethod(b,b,c,d,e);
		
		setExpectedMatches(new boolean[] {true,false,false,true,true,true,true,false,true,true,false});
		myMethod(reallyB,b,c,d,e);
		
		setExpectedMatches(new boolean[] {true,false,false,true,true,false,false,false,true,true,true});
		myMethod(a,b,reallyD,d,e);
		
		setExpectedMatches(new boolean[] {true,false,false,true,true,false,false,false,true,true,true});
		myMethod(a,b,reallyD,reallyE,e);
	}
	
	public static void myMethod(A a, B b, C c, D d, E e) {
		return;
	}
	
}

@MyClassRetentionAnnotation
class A {
  public void doSomething() {}
}


@MyAnnotation
class B extends A {
  public void doSomething() {}
}

class C {}

@MyInheritableAnnotation
@MyAnnotation
class D extends C {
  public void doSomething() {}
}

class E extends D {
  public void doSomething() {}
}


@interface MyClassRetentionAnnotation {}

@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@interface MyAnnotation {}

@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Inherited
@interface MyInheritableAnnotation {}