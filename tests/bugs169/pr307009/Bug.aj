public aspect Bug {
	// works when specifying *.aj *.java, fails when using -sourceroots!
	declare soft : Exception : call(@Ann * *(..));
	// this works in both cases!
	//declare soft : Exception : call(* m2(..));
}
