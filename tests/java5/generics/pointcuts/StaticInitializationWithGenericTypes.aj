public aspect StaticInitializationWithGenericTypes {
	
	declare warning 
	   : staticinitialization<T>(GenericInterface<T>+) 
	   : "one generic param, wrong bounds";
	
	declare warning 
	   : staticinitialization<T>(GenericInterface<T extends Number>+)
	   : "one generic param, correct bounds";
	
	declare warning 
	    : staticinitialization<X>(GenericInterface<X extends Number>+)
		: "doesn't matter what type variable name you use";
	
	declare warning
	    : staticinitialization<E>(GenericImplementingClass<E extends Number>)
	    : "works with classes too";
	    
	declare warning
		: staticinitialization<A,B>(GenericImplementingClass<A extends Number,B>)
		: "wrong number of type vars";
		
    declare warning
    	: staticinitialization<N>(GenericImplementingClass<N extends Number & Comparable>)
    	: "bounds not matching on interface";
	
}