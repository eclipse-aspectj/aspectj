public aspect LTWDeclareWarning {
	
	pointcut method () :
		execution(* LTWHelloWorld.println(..));
	
	declare warning : method () :
		"LTWDeclareWarning.println()";

}
