
public aspect AspectToMatch {

	//declare parents : ((@Entity *) && !hasmethod(@SearchAnnotation public * get*()) && !hasfield(@SearchAnnotations * *)) implements InterfaceToAdd;
	declare parents : ((@Entity *) && !hasmethod(@SearchAnnotation public * get*()) && 
			            !hasfield(@SearchAnnotation * *)) implements InterfaceToAdd;
	
	/*
	declare warning :
		staticinitialization(
			!hasmethod(* getMamma())
			) : "Not found";
	*/
	/*
	before() : handler(			
			(@Entity *) && !(hasmethod(@SearchAnnotation public * get*())) && !(hasfield(@SearchAnnotations * *))
			) {
		
		
	}
	*/
	
	/*
	before() : execution(!@(org.aspectj.bug*) public * get*()) {
		
	}
	*/

}
