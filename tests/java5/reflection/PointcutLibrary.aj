// random collection of pointcuts to check that
// reflective world and PointcutParser can interpret
// them correctly.

public aspect PointcutLibrary {
	
	public pointcut propertyAccess() : get(* *);
	public pointcut propertyUpdate() : set(* *);
	public pointcut methodExecution() : execution(* *(..));
	public pointcut propertyGet() : execution(!void get*(..));
	public pointcut propertySet(Object newValue) 
		: execution(void set*(..)) && args(newValue);
	
}