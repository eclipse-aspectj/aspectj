aspect A6 {
	
	pointcut p(Integer value) : set(Integer memory) && args(value); 
	
}
