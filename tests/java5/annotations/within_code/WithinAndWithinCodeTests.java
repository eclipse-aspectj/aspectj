public aspect WithinAndWithinCodeTests {
	
	// should be two matches, L32 and L39
	declare warning : execution(* doSomething(..)) && @within(MyAnnotation)
	                : "@within match on non-inherited annotation";
	
	// one match on L39
	declare warning : execution(* doSomething(..)) && @within(MyInheritableAnnotation)
	                : "@within match on inheritable annotation";
	
	// one match on L32
	declare warning : call(* doSomething(..)) && @withincode(MyClassRetentionAnnotation)
	                : "@withincode match";	
}