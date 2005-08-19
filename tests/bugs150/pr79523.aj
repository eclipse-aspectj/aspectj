public aspect pr79523 {
	
	pointcut someCalls(String str) : call(*.new(String)) && args(str);
	declare warning : someCalls(str) : "not allowed"; // CE L 4
	
}