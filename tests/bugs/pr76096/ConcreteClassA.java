// In the ConcreteClassA.someMethod() method, the creation of the anonymous class should
// be ok as the ITD ensures that InterfaceA.a2() is implemented.

interface InterfaceA {
 public void a1();
 public void a2();
}

abstract class AbstractClassA implements InterfaceA {
  public void a1() {
    System.out.println("AbstractClassA.a()");
  }
}

public class ConcreteClassA extends AbstractClassA {
  public void someMethod() {
    InterfaceA a = new AbstractClassA() {  };
    a.a2(); 
  }
  
  public static void main(String[]argv) {
  	new ConcreteClassA().someMethod();
  	new concCB().someMethod();
  }
}

aspect IntroAspectA {
  public void AbstractClassA.a2() {
    System.out.println("AbstractClassA.a2() from IntroAspectA");
  }
}

interface IB {
	 public void m2();
}

abstract class absCB implements IB {
	public void m1() { }
}

class concCB extends absCB {
	public void someMethod() {
		IB b = new IB() {};
		b.m2();
	}
}

aspect introAspectB {
	public void IB.m2() {System.err.println("absCB.m1() from IB");}
}
