aspect DeclareWarnings {
	
	pointcut p() : execution(* C.amethod());
	
	declare warning : p() : "warning 1";
	declare warning : p() : "warning 2";
	declare warning : p() : "warning 3";
	declare warning : p() : "warning 4";
	declare warning : p() : "warning 5";
	declare warning : p() : "warning 6";
	declare warning : p() : "warning 7";
	declare warning : p() : "warning 8";
	declare warning : p() : "warning 9";
	declare warning : p() : "warning 10";
	
}

class C {
	
	public void amethod() {}
}
