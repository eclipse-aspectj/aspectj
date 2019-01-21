public aspect MyAspect {
	before() : execution(* Application.*(..)) {
		System.out.println(this.getClass().getName() + " -> " + thisJoinPointStaticPart);
	}
}
