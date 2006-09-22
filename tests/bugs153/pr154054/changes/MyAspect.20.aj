public aspect MyAspect {

	pointcut mypointcut(): execution(* getX()) && !within(MyAspect);

	int around(): mypointcut() {
		int w = proceed() + 4;
		return w;	
	}

}
