// from Bug#:  29959
import org.aspectj.testing.Tester;

aspect Foo {
    String A.onlyA() { return "onlyA"; }
    String A.foo() { return "Afoo"; }
    String B.foo() { return super.foo() + ":" + onlyA() + ":" + super.getName(); }
}

class A {
	String getName() { return "A"; }
}
class B extends A {
	String getName() { return "B"; }
	
	String onB1() { return foo() + ":" + onlyA() + ":" + getName(); }
	String onB2() { return super.foo() + ":" + super.onlyA() + ":" + super.getName(); }
}

public class SuperToIntro {
  public static void main(String[] args) {
  	B b = new B();
  	Tester.checkEqual(b.foo(), "Afoo:onlyA:A");
  	Tester.checkEqual(b.onB1(), "Afoo:onlyA:A:onlyA:B");
  	Tester.checkEqual(b.onB2(), "Afoo:onlyA:A");
  }
}
