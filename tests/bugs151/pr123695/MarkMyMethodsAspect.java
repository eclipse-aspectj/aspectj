

public aspect MarkMyMethodsAspect {
	
	/* All methods not marked with @Read nor @Write are marked with @Write
	 * 
	 * When @MarkMyMethods is present on a Type, all public methods of
	 * that type must either be marked with @Read or @Write. If neither of 
	 * @Read or @Write is present on such a method, the method is automatically
	 * annotated with the default marker, i.e. @Write
	 * 
	 * *******************************************************
	 * BUG 
	 * internal null pointer exception with the first part
	 * of the declare statement.
	 * *******************************************************
	 * 
	 */
	declare @method : !@(Write || Read) public !static * (@MarkMyMethods *).*(..) : @Write;
	
	// This one works
	//declare @method : !@(Read) public !static * (@MarkMyMethods *).*(..) : @Write;
	// This one too
	//declare @method : !@(Write) public !static * (@MarkMyMethods *).*(..) : @Write;
	
	
	/* Cannot have @Read or @Write methods without @MarkMyMethods 
	 *
	 * When @Read or @Write is present on a method, the enclosing type must
	 * have the @AccessClassified annotation.
	 */
	declare error : execution(@Read public * !@MarkMyMethods *.*(..)) :
		"Cannot have public @Read methods inside non @AccessClassified types.";
	declare error : execution(@Write public * !@MarkMyMethods *.*(..)) :
		"Cannot have public @Write methods inside non @AccessClassified types.";
	
	/* Cannot have a method marked with both @Read and @Write
	 *  
	 * What would be necessary is to have an annotation that can take
	 * a parameter to identify which type of access is needed that would prevent
	 * the user from having the 2 at the same time e.g. @Access(READ). Unfortunately,
	 * AspectJ 1.5 can currently only work with marker annotations and ignores
	 * parameter annotations.
	 */
	declare error : readMethod() && writeMethod() :
		"Cannot have both @Read and @Write on the same method.";
	
	/*
	 * public @Read methods inside @MarkMyMethods types
	 */ 
	public pointcut readMethod() : 
		execution(@Read public !static * @MarkMyMethods *.*(..));
	
	/*
	 * public @Write methods inside @MarkMyMethods types
	 */
	public pointcut writeMethod() :
		execution(@Write public !static * @MarkMyMethods *.*(..));
	
}
