import java.util.List;

public aspect ThisOrTargetTests {
	
  pointcut doSomethingExecution() : execution(* doSomething());
  pointcut doSomethingCall() : call(* doSomething());
  
  before() : doSomethingExecution() && @this(@MyAnnotation) {
  	// should match:
  	// b.doSomething(), reallyB.doSomething() [with test],
  	// c.doSomething()
  	System.out.println("@this(@MyAnnotation): " + thisJoinPointStaticPart);
  }
  
  before() : doSomethingExecution() && @this(@MyInheritableAnnotation) {
  	// should match:
  	// c.doSomething()
  	// d.doSomething()
  	// reallyD.doSomething()
  	System.out.println("@this(@MyInheritableAnnotation): " + thisJoinPointStaticPart);
  }
  
  after() returning : doSomethingCall() && @target(@MyAnnotation) {
  	// should match:
  	// b.doSomething(), reallyB.doSomething() [with test],
  	// c.doSomething()
  	System.out.println("@target(@MyAnnotation): " + thisJoinPointStaticPart);
  }
  
  after() returning : doSomethingCall() && @target(@MyInheritableAnnotation) {
  	// should match:
  	// c.doSomething()
  	// d.doSomething()
  	// reallyD.doSomething()
  	System.out.println("@target(@MyInheritableAnnotation): " + thisJoinPointStaticPart);
  }
  	
}