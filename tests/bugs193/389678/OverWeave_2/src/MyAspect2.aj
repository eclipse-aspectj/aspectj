public aspect MyAspect2 {
	before() : execution(* *(..)) {
		System.out.println(this.getClass().getName() + " -> " + thisJoinPointStaticPart);
	}
}
