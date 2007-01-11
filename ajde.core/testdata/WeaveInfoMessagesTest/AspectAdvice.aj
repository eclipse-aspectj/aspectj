// Simple aspect that tramples all over Simple.java

public aspect AspectAdvice {

  // Go through all the kinds of things that might affect a type:
  pointcut methodRunning(): execution(* *(..)) && !execution(* main(..));
  
  before(): methodRunning() {
  	System.err.println("BEFORE ADVICE");
  }
  
  after(): methodRunning() {
  	System.err.println("AFTER ADVICE");
  }

  after() returning: methodRunning() {
	System.err.println("AFTER RETURNING ADVICE");
  }

  after() throwing : methodRunning() {
	System.err.println("AFTER THROWING ADVICE");
  }
  
  void around(): execution(* main(..)) && !cflow(adviceexecution()){
	System.err.println("AROUND ADVICE");
  	proceed();
  }
 
 
  interface markerInterface {	
  }	
}
