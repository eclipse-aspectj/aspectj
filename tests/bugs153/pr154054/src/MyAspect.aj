public aspect MyAspect {

	pointcut mypointcut(): execution(* getX()) && !within(MyAspect);

	int around(): mypointcut() {
		int w = proceed() + 3;
		return w;	
	}

}
