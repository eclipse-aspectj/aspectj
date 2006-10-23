public aspect AdviceDidNotMatch {
	
	before() : execution(* *.*(..)) {
	}
	
}
