public aspect Audit {

	// An inter-type declaration: its source location is recorded in the woven class too
	int Target.extra;

	before(): execution(* Target.run()) {
		System.out.println("audit");
	}
}
