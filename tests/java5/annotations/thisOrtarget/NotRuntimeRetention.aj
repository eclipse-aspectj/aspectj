public aspect NotRuntimeRetention {
    
    pointcut doSomethingExecution() : execution(* doSomething());
    pointcut doSomethingCall() : call(* doSomething());
    
    // CE L7
    before() : doSomethingExecution() && @this(@MyClassRetentionAnnotation) {
    	// should be compile-time error!
        System.out.println("How did I get here?");
    }
    
    // CE L13
    after() returning : doSomethingCall() && @target(@MyClassRetentionAnnotation) {
    	// should be compile-time error!
        System.out.println("How did I get here?");
    }
    
}