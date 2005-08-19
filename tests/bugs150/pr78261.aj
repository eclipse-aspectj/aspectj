public aspect pr78261 {
	
	pointcut absurd() : get(void *);  // CE L3, fields can't be void
	
}