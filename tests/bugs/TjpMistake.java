/** @testcase PR#75129 NPE on thisJoinPoint mistake */
public class TjpMistake {
public static void main(String[] a) {
   new C().go();
   new D().go();
   new E().go();
}
}
interface I { void go();}
class C {}
class D { 
  public void go() { System.out.println("D.go() in " + this); }
}
class E extends D { 
}
aspect A {
  declare parents: (C || D) implements I;
  public void I.go() { System.out.println("I.go() in " + this); }
  before() : execution(* I.*(..)) { 
	System.out.println(
	// mistake caused compiler crash rather than error
	thisJoinPoint.getSignature.toLongString()  // CE 22
	// correct
	//thisJoinPointStaticPart.getSignature().toLongString() 
	+ " " 
	+ thisJoinPoint.getSignature().getDeclaringType() 
	+ " " 
	+ this); }
}
