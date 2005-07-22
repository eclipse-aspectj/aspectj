import java.io.Serializable;
public aspect StaticInitializationWithGenericTypesAdvanced {
	
	// basic bounds
	declare warning 
		: staticinitialization<T>(JustCallMeGeneric<T>)
		: "simple match";
		
	declare warning
		: staticinitialization<R>(JustCallMeGeneric<R extends Object>)
		: "matches since R and R extends Object are equivalent";
	
	// interface bounds
	declare warning 
		: staticinitialization(ClassWithInterfaceBounds)
		: "raw type should match";
		
	declare warning
		: staticinitialization<X>(ClassWithInterfaceBounds<X>) // CW L19
		: "unbound type variable does not match";
	
	declare warning
		: staticinitialization<Y>(ClassWithInterfaceBounds<Y extends Number>) // CW L23
		: "upper bound match on its own is not enough";
		
    declare warning
    	: staticinitialization<Z>(ClassWithInterfaceBounds<Z extends Number & Comparable>) // CW L27
    	: "still no match, wrong number of i/f bounds";
    	
    declare warning
    	: staticinitialization<A>(ClassWithInterfaceBounds<A extends Number & Comparable & Serializable>)
    	: "matches all bounds";
    	
    declare warning 
        : staticinitialization<B>(ClassWithInterfaceBounds<B extends Number & Serializable & Comparable>)
        : "still matches with interfaces specified in a different order";
	
	// type variable inter-dependencies
	declare warning
		: staticinitialization<A,B>(TypeVariablesTiedInKnots<A,B>)  // CW L40
		: "no match, wrong upper bound on B";
		
	declare warning
		: staticinitialization<C,D>(TypeVariablesTiedInKnots<C,D extends C>)
		: "matches with type variable inter-dependencies";
	
	
	// wildcards in patterns
	declare warning 
		: staticinitialization<T>(*<T>)
		: "matches any generic type with one unbound type var";
		
	declare warning
		: staticinitialization<S>(*<S extends Number+>)
		: "any generic type with one type var bound to Number or subtype";
		
	declare warning
		: staticinitialization<R>(*<R extends *>)
		: "matches a generic type with any upper bound and i/f bounds";
	
}

class ClassWithInterfaceBounds<T extends Number & Comparable & Serializable> {
	
	T really;
	
}

class TypeVariablesTiedInKnots<S, T extends S> {
  
	S club;
	T later;
	
}

class JustCallMeGeneric<T> {
	
	T simple;
	
}

class MinesADouble<D extends Double> {

	D orQuit;
}