public abstract aspect Tracing {
	
	before (Object obj) : execution(* *(..)) && this(obj) {
		System.out.println(thisJoinPoint);
	}
	
//	before (Object obj) : execution(* *(..)) && this(obj) {
	before () : execution(* *(..)) {
		System.out.println(thisJoinPointStaticPart);
	}
	
//	before (Object obj) : execution(* *(..)) && this(obj) {
	before () : execution(* *(..)) && this(Object) {
		System.out.println(thisEnclosingJoinPointStaticPart);
	}
}
