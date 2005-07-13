public aspect ParameterizedTypesInAtPCDs {
	
	public pointcut atthis() : @this(List<String>);
	
	public pointcut attarget() : @target(List<String>);
	
	public pointcut atargs() : @args(List<String>);
	
	public pointcut atannotation() : @annotation(List<String>);
	
	public pointcut atwithin() : @within(List<String>);
	
	public pointcut atwithincode() : @withincode(List<String>);
	
}