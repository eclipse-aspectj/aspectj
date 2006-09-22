public aspect MyAspect {

	pointcut mypointcut(): execution(* getName()) && !within(MyAspect);

	String around(): mypointcut() {
		String w = proceed() + " and George";
		return w;	
	}

}
