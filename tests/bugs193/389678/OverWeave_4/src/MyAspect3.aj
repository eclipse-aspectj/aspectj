public aspect MyAspect3 {
	before() : execution(* *(..)) {
		System.out.println(this.getClass().getName() + " -> " + thisJoinPointStaticPart);
	}
}
