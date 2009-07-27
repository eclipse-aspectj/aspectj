package test;

public aspect Logger {

	//declare precedence: Logger, ErrorContainment; 
//	
//	before() : adviceexecution() && !within(Logger){
//		
//		System.out.println("--->Logger:" + thisJoinPoint);
//		
//	}
//	
//	after() : adviceexecution() && !within(Logger) {
//		
//		System.out.println("--->Logger:" + thisJoinPoint);
//		
//	}
	
	
	before(): execution(* hk..*.do*(..)){
		System.out.println("This is a test");
	}
	
}
