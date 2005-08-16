public aspect pr107059 {
	
	before() : call(void (@a *)(..)) {}  // note missing "." in pattern
	
}