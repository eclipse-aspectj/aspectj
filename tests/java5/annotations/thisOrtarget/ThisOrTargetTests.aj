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
  
  before() : doSomethingExecution() && @this(@MyClassRetentionAnnotation) {
  	// should be compile-time error!
  }
  
  before() : doSomethingExecution() && @this(@MyInheritableAnnotation) {
  	// should match:
  	// c.doSomething()
  	// d.doSomething()
  	// reallyD.doSomething()
  	System.out.println("@this(@MyInheritableAnnotation): " + thisJoinPointStaticPart);
  }
  
  after() returning : doSomthingCall() && @target(@MyAnnotation) {
  	// should match:
  	// b.doSomething(), reallyB.doSomething() [with test],
  	// c.doSomething()
  	System.out.println("@target(@MyAnnotation): " + thisJoinPointStaticPart);
  }
  
  after() returning : doSomethingCall() && @target(@MyClassRetentionAnnotation) {
  	// should be compile-time error!
  }
  
  after() returning : doSomethingCall() && @target(@MyInheritableAnnotation) {
  	// should match:
  	// c.doSomething()
  	// d.doSomething()
  	// reallyD.doSomething()
  	System.out.println("@target(@MyInheritableAnnotation): " + thisJoinPointStaticPart);
  }
  
  after(MyAnnotation ann) returning : @target(ann) {
  	// should be compile time error (limitation)
  }
	
}