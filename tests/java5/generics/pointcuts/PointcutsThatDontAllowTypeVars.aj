public aspect PointcutsThatDontAllowTypeVars {
	
	public pointcut handlerWithVars() : handler<T>(*);
	
	public pointcut cflowWithVars() : cflow<T>(ifWithVars());
	
	public pointcut cflowbelowWithVars() : cflowbelow<S>(ifWithVars());
	
	public pointcut thisWithVars() : this<T>(String);
	
	public pointcut targetWithVars() : target<T>(String);
	
	public pointcut argsWithVars() : args<T>(String);
	
	public pointcut atthisWithVars() : @this<T>(*);
	
	public pointcut attargetWithVars() : @target<T>(*);
	
	public pointcut atargsWithVars() : @args<T>(*);
	
	public pointcut atwithinWithVars() : @within<T>(*);
	
	public pointcut atwithincodeWithVars() : @withincode<T>(*);
	
	public pointcut atannotationWithVars() : @annotation<T>(*);

	public pointcut ifWithVars() : if<T>(true); // message for this one should be improved...
}