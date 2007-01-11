// Simple aspect that tramples all over Simple.java

public aspect AspectDeclareExtends {
	
  pointcut methodRunning(): execution(* *(..));
  
  declare parents: Simple extends InTheWay;
  
//  declare soft: 
//  
//  declare precedence:
//  
//  declare warning:
//  
//  declare error:
//  
}

  
interface MarkerInterface {}

class InTheWay {}
