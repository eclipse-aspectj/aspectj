public aspect LTWDeclareError {
	
	pointcut method () :
		execution(* LTWHelloWorld.println(..));
	
	declare error : method () :
		"LTWDeclareError.println()";

}
