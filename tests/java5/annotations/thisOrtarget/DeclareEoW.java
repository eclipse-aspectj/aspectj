public aspect DeclareEoW {
	
	declare warning : @this(MyAnnotation) : "should give compilation error";
	
	declare error : @target(MyAnnotation) : "should give compilation error";
	
}