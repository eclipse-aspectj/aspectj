public aspect InitializationPointcutMatchingParamTypes {
	
	// rule 3) a raw parameter pattern matches any parameterized type
	declare warning : initialization(Generic.new(java.util.List)) 
	                  : "raw param type matching in init ok";
	declare warning : preinitialization(Generic.new(java.util.List))
	                  : "raw param type matching in preinit ok";
	
	// rule 4) A param type declared using a type variable is matched by its erasure
	declare warning : initialization(Generic.new(Object)) : "erasure matching in init ok";
	declare warning : preinitialization(Generic.new(Object)) : "erasure matching in preinit ok";
	declare warning : initialization(Generic.new(java.util.List<Object>)) : "does not match! erasure is List";
	declare warning : preinitialization(Generic.new(java.util.List<Object>)) : "does not match! erasure is List";
	declare warning : initialization(Generic.new(java.util.ArrayList)) : "erasure matching in init with params ok";
	declare warning : preinitialization(Generic.new(java.util.ArrayList)) : "erasure matching in preinit with params ok";
	
	// rule 5) A param type declared using a parameterized type is matched by parameterized type patterns
	declare warning : initialization(UglyBuilding.new(java.util.List<String>)) : "parameterized type matching in init ok";
	declare warning : preinitialization(UglyBuilding.new(java.util.List<String>)) : "parameterized type matching in preinit ok";
	declare warning : initialization(UglyBuilding.new(java.util.Map<Number,String>)) : "parameterized type matching in init ok x2";
	declare warning : preinitialization(UglyBuilding.new(java.util.Map<Number,String>)) : "parameterized type matching in preinit ok x2";
	
	// rule 6) generic wildcards match exactly, aspectj wildcards match wildly
	declare warning : initialization(UglyBuilding.new(java.util.List<?>,int)) : "wildcard init matching ok";
	declare warning : preinitialization(UglyBuilding.new(java.util.List<?>,int)) : "wildcard preinit matching ok";
	declare warning : initialization(UglyBuilding.new(java.util.List<? extends Number>,double)) : "wildcard extends init matching ok";
	declare warning : preinitialization(UglyBuilding.new(java.util.List<? extends Number>,double)) : "wildcard extends preinit matching ok";
	declare warning : initialization(UglyBuilding.new(java.util.List<? super Double>,float)) : "wildcard super init matching ok";
	declare warning : preinitialization(UglyBuilding.new(java.util.List<? super Double>,float)) : "wildcard super preinit matching ok";
	
	declare warning : initialization(UglyBuilding.new(java.util.List<*>,..)) : "the really wild show";
}


class Generic<T> {
	public Generic(java.util.List<String> ls) {}
	public Generic(T t) {}
	public Generic(java.util.ArrayList<T> ts) {}
}

interface ISore<E> {
	
	void iSee(E anE);
	
}

class UglyBuilding {
	public UglyBuilding(java.util.List<String> ls) {}
	public UglyBuilding(java.util.Map<Number,String> mns) {}
	public UglyBuilding(java.util.List<?> ls, int i) {}
	public UglyBuilding(java.util.List<? extends Number> ln, double d) {}
	public UglyBuilding(java.util.List<? super Double> ln, float f) {}
}