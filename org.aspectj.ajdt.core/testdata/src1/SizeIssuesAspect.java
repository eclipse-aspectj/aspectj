aspect SizeIssuesAspect {

  public pointcut rasScope() : within(*);
  
  pointcut toStringMethod() : execution(* *.toString());
  
  pointcut publicMethods() : rasScope() &&
	execution( public * *(..)) && !toStringMethod();
    
	after() returning:  publicMethods() {
	  System.err.println(thisJoinPointStaticPart.getSignature().toLongString());
	}
}

