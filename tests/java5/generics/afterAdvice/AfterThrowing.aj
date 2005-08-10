public aspect AfterThrowing {
	
	// since a generic type may not be a subtype of throwable, this is always an
	// error.
	
	after() throwing(java.util.List<String> ls) : execution(* *(..)){
		
	}
	
}