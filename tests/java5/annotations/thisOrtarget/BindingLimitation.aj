public aspect BindingLimitation {
	
  pointcut doSomethingExecution() : execution(* doSomething());
  pointcut doSomethingCall() : call(* doSomething());
  
  after(MyAnnotation ann) returning : @target(ann) && doSomethingCall() {
  	// should be compile time error (limitation)
  }
	
}