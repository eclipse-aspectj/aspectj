public aspect Aspect {

	declare @field: * Hello.dummy : -@MyAnnotation;
	declare @field: * Hello.dummy : @MyAnnotation(dummy2 = "korte");

}
